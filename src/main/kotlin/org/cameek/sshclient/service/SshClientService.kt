package org.cameek.sshclient.service

import org.apache.commons.lang3.StringUtils
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.channel.Channel
import org.cameek.sshclient.bean.CmdStrIOE
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.util.EnumSet
import java.util.concurrent.TimeUnit

@Component
class SshClientService {

    companion object {
        private val log = LoggerFactory.getLogger(SshClientService::class.java)

        private val IGNORE_STRINGS = arrayOf("tput[:] unknown terminal [\"]dummy[\"](\r?)(\n?)")

        private val WAIT_FOR_EVENTS = EnumSet.of(
            ClientChannelEvent.CLOSED,
            ClientChannelEvent.EXIT_STATUS,
            ClientChannelEvent.EOF,
            ClientChannelEvent.EXIT_SIGNAL,
            ClientChannelEvent.STDERR_DATA,
            ClientChannelEvent.STDOUT_DATA,
            ClientChannelEvent.TIMEOUT
        )

        private const val LOG_ABBREVIATE_MAX = 1000
    }

    fun remoteCommand(
        username: String, password: String,
        host: String, port: Int,
        defaultTimeoutSeconds: Long,
        command: String
    ): String {

        var result: String

        log.debug("remoteCommand(...) - Begin, command=$command")

        val client = SshClient.setUpDefaultClient()
        client.start()

        try {
            client
                .connect(username, host, port)
                .verify(defaultTimeoutSeconds, TimeUnit.SECONDS)

                .session.use { session ->

                    log.debug("session - Begin")

                    session.addPasswordIdentity(password)
                    session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS)

                    ByteArrayOutputStream().use { responseStream ->

                        result = createChannelAndExecute(session, responseStream, defaultTimeoutSeconds, command)

                    }

                    log.debug("session - End")
                }
        } finally {
            client.stop()
        }

        log.debug("remoteCommand(...) - End, return result=\"${StringUtils.abbreviate(result, LOG_ABBREVIATE_MAX)}\"")

        return result

    }

    fun remoteCommands(
        username: String, password: String,
        host: String, port: Int,
        defaultTimeoutSeconds: Long,
        commands: Iterable<String>
    ): Iterable<CmdStrIOE> {

        val results = mutableListOf<CmdStrIOE>()

        log.debug("remoteCommand(...) - Begin, command=$commands")

        val client = SshClient.setUpDefaultClient()
        client.start()

        try {
            client
                .connect(username, host, port)
                .verify(defaultTimeoutSeconds, TimeUnit.SECONDS)

                .session.use { session ->

                    log.debug("session - Begin")

                    session.addPasswordIdentity(password)
                    session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS)

                    ByteArrayOutputStream().use { responseStream ->

                        for (command in commands) {
                            val result = createChannelAndExecute(session, responseStream, defaultTimeoutSeconds, command)
                            results.add(CmdStrIOE(input = command, output = result))
                        }

                    }

                    log.debug("session - End")
                }
        } finally {
            client.stop()
        }

        log.debug("remoteCommand(...) - End, return results=\"${results}\"")

        // Return immutable list
        return results.toList()

    }

    private fun createChannelAndExecute(
        session: ClientSession,
        responseStream: ByteArrayOutputStream,
        defaultTimeoutSeconds: Long,
        command: String
    ): String {

        log.debug("createChannelAndExecute(...) - Begin, command=\"$command\"")

        var result = ""
        session.createChannel(Channel.CHANNEL_SHELL).use { channel ->
            channel.setOut(responseStream)
            try {
                channel.open().verify(defaultTimeoutSeconds, TimeUnit.SECONDS)
                channel.invertedIn.use { pipedIn ->
                    pipedIn.write(command.toByteArray())
                    pipedIn.flush()
                }
                channel.waitFor(
                    WAIT_FOR_EVENTS,
                    TimeUnit.SECONDS.toMillis(defaultTimeoutSeconds)
                )

                val responseString = responseStream.toString()

                result = filterResponseStr(responseString)

            } finally {
                channel.close(false)
            }
        }

        log.debug("createChannelAndExecute(...) - End, return result=\"${StringUtils.abbreviate(result, LOG_ABBREVIATE_MAX)}\"")

        return result
    }

    fun filterResponseStr(responseStr: String): String {

        log.debug("filterResponseStr(...) - Begin, responseStr=\"${StringUtils.abbreviate(responseStr, LOG_ABBREVIATE_MAX)}\"")

        var filteredResponseString = responseStr
        for (str in IGNORE_STRINGS) {
            filteredResponseString = filteredResponseString.replace(str.toRegex(), "")
        }

        log.debug("filterResponseStr(...) - End, return result=\"${StringUtils.abbreviate(filteredResponseString, LOG_ABBREVIATE_MAX)}\"")

        return filteredResponseString
    }

}
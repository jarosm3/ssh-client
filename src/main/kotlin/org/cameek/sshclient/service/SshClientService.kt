package org.cameek.sshclient.service

import org.apache.commons.lang3.StringUtils
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.common.channel.Channel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.util.EnumSet
import java.util.concurrent.TimeUnit

@Component
class SshClientService {

    companion object {
        private val log = LoggerFactory.getLogger(SshClientService::class.java)
        private val IGNORE_STRS = arrayOf("tput[:] unknown terminal [\"]dummy[\"](\r?)(\n?)")

        private val WAIT_FOR_EVENTS = EnumSet.of(
            ClientChannelEvent.CLOSED,
            ClientChannelEvent.EXIT_STATUS,
            ClientChannelEvent.EOF,
            ClientChannelEvent.EXIT_SIGNAL,
            ClientChannelEvent.STDERR_DATA,
            ClientChannelEvent.STDOUT_DATA
        )
    }

    fun remoteCommand(
        username: String, password: String,
        host: String, port: Int,
        defaultTimeoutSeconds: Long,
        command: String
    ): String {

        var result = "";

        log.debug("remoteCommand(...) - Begin")

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
                    }

                    log.debug("session - End")
                }
        } finally {
            client.stop()
        }

        log.debug("remoteCommand(...) - End, Result: ${StringUtils.abbreviate(result, 100)}");

        return result;

    }

    fun filterResponseStr(responseStr: String): String {

        log.debug("filterResponseStr(${StringUtils.abbreviate(responseStr, 100)}) - Begin")

        var filteredResponseString = responseStr
        for (str in IGNORE_STRS) {
            filteredResponseString = filteredResponseString.replace(str.toRegex(), "")
        }

        log.debug("filterResponse(${StringUtils.abbreviate(responseStr, 100)}) - End, "
                + "Result: ${StringUtils.abbreviate(filteredResponseString, 100)}")

        return filteredResponseString;
    }

}
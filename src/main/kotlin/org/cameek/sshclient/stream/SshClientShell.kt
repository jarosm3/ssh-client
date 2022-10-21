@file:Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")

package org.cameek.sshclient.stream

import kotlinx.coroutines.*
import org.apache.commons.text.StringEscapeUtils
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.channel.Channel
import org.apache.sshd.scp.client.DefaultScpClient
import org.cameek.sshclient.bean.CmdStrIOE
import org.cameek.sshclient.event.*
import org.cameek.sshclient.service.filter.BasicSshFilter
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.Predicate

class SshClientShell(

    val host: String,
    val port: Int,
    val timeoutMillis: Long = 10000,
    val username: String,
    val password: String,
    val endOfLine: String = "\n",
    val charset: Charset = Charsets.UTF_8

): Closeable {

    private val client: SshClient
    private val clientSession: ClientSession
    private val clientChannel: ClientChannel
    private val outputStream: InputStream
    private val errorStream: InputStream
    private val inputStream: OutputStream

    companion object {
        private val log = LoggerFactory.getLogger(SshClientShell::class.java)
    }

    init {
        log.debug("init - Begin")

        log.info("Initializing SSH client shell")

        client = SshClient.setUpDefaultClient()
        client.start()

        clientSession = client.connect(username, host, port)
            .verify(timeoutMillis, TimeUnit.MILLISECONDS)
            .session
        clientSession.addPasswordIdentity(password)
        clientSession.auth().verify(timeoutMillis, TimeUnit.MILLISECONDS)

        //outputStream = ByteArrayOutputStream()
        //errorStream = ByteArrayOutputStream()

        clientChannel = clientSession.createChannel(Channel.CHANNEL_SHELL)
        clientChannel.open().verify(timeoutMillis, TimeUnit.MILLISECONDS)

        //clientChannel.setOut(outputStream)
        //clientChannel.setErr(errorStream)

        inputStream = clientChannel.invertedIn
        outputStream = clientChannel.invertedOut
        errorStream = clientChannel.invertedErr

        log.debug("init - End")
    }

    fun processCommand(
        command: CmdStrIOE,
        outputFilter: Predicate<String> = BasicSshFilter.singleton,
        errorFilter: BasicSshFilter = BasicSshFilter.singleton,
        listener: Listener = EmptyListener.singleton,
    ): CmdStrIOE {
        log.debug("processCommand() - Begin")

        log.info("Processing command in SSH client shell")

        var outputString = ""
        var errorString = ""

        runBlocking {

            launch(Dispatchers.IO) {
                log.debug("Input Thread for Sending to SSH - Begin")

                val lines = command.input.split(endOfLine)
                var i = 0
                for (line in lines) {

                    if ((i == (lines.size - 1)) && (line.isNotEmpty())) {
                        log.debug("Skipping last portion of input split because it's empty")
                        break

                    }

                    if (log.isDebugEnabled) {
                        val printLine = StringEscapeUtils.escapeJava(line + endOfLine)
                        log.debug("Sending to SSH STDIN >> \"$printLine\"")
                    }

                    inputStream.write(line.toByteArray(charset))
                    inputStream.write(endOfLine.toByteArray(charset))
                    listener.onEvent(InputLineEvent(line))
                    i++
                }

                inputStream.flush()

                log.debug("Input Thread for Sending to SSH - End")
            }

            launch(Dispatchers.IO) {
                log.debug("Output Thread for Receiving from SSH - Begin")

                outputStream.reader(charset).use {
                    reader ->
                        reader.forEachLine {
                            line ->

                                if (log.isDebugEnabled) {
                                    val printLine = StringEscapeUtils.escapeJava(line + endOfLine)
                                    log.debug("Received from SSH STDOUT << \"$printLine\"")
                                }

                                // If filter predicate returns false, ignore such a line
                                if (!outputFilter.test(line)) {
                                    if (log.isDebugEnabled) {
                                        val printLine = StringEscapeUtils.escapeJava(line + endOfLine)
                                        log.debug("Ignoring line from SSH STDOUT \"$printLine\"")
                                    }
                                }

                                // Accept the line
                                else {
                                    listener.onEvent(OutputLineEvent(line))
                                    outputString = outputString + line + endOfLine
                                }
                        }
                }

                log.debug("Output Thread for Receiving from SSH - End")
            }

            launch(Dispatchers.IO) {
                log.debug("Error Thread for Receiving from SSH - Begin")

                errorStream.reader(charset).use {
                    reader ->
                        reader.forEachLine {
                            line ->

                                if (log.isDebugEnabled) {
                                    val printLine = StringEscapeUtils.escapeJava(line + endOfLine)
                                    log.debug("Received from SSH STDERR << \"$printLine\"")
                                }

                                // If filter predicate returns false, ignore such a line
                                if (!errorFilter.test(line)) {
                                    if (log.isDebugEnabled) {
                                        val printLine = StringEscapeUtils.escapeJava(line + endOfLine)
                                        log.debug("Ignoring line from SSH STDERR \"$printLine\"")
                                    }
                                }

                                // Accept the line
                                else {
                                    listener.onEvent(ErrorLineEvent(line))
                                    errorString = errorString + line + endOfLine
                                }
                        }
                }

                log.debug("Error Thread for Receiving from SSH - End")
            }

        }

        val result = command.copy(output = outputString, error = errorString)

        log.debug("processCommand() - End, Return result=$result")

        return result
    }

    override fun close() {
        log.debug("close() - Begin")

        log.info("Closing SSH client shell")

        try {
            inputStream.close()
        } catch (t: Throwable) {
            log.error("Close input stream: $t.message", t)
        }

        try {
            outputStream.close()
        } catch (t: Throwable) {
            log.error("Close output stream: $t.message", t)
        }

        try {
            errorStream.close()
        } catch (t: Throwable) {
            log.error("Close error stream: $t.message", t)
        }

        try {
            clientChannel.close()
        } catch (t: Throwable) {
            log.error("Close client channel: $t.message", t)
        }

        try {
            clientSession.close()
        } catch (t: Throwable) {
            log.error("Close client session: $t.message", t)
        }

        try {
            client.close()
        } catch (t: Throwable) {
            log.error("Close client: $t.message", t)
        }

        log.debug("close() - End")
    }

}
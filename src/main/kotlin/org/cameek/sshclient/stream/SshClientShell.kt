package org.cameek.sshclient.stream

import kotlinx.coroutines.*
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.channel.Channel
import org.cameek.sshclient.bean.CmdStrIOE
import org.cameek.sshclient.event.*
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class SshClientShell(

    val host: String,
    val port: Int,
    val timeoutMillis: Long = 10000,
    val username: String,
    val password: String,
    val command: CmdStrIOE,  // TODO: here should be CommandProvider (SequentialCmdProvider, FlowCmdProvider)
    val listener: Listener = EmptyListener(),
    val endOfLine: String = "\n",
    val charset: Charset = Charsets.UTF_8

): Closeable {

    private val client: SshClient
    private val clientSession: ClientSession
    private val clientChannel: ClientChannel
    private val outputStream: InputStream
    private val errorStream: InputStream
    private val inputStream: OutputStream

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
        clientChannel.open().verify(timeoutMillis, TimeUnit.MILLISECONDS);
        //clientChannel.setOut(outputStream)
        //clientChannel.setErr(errorStream)

        inputStream = clientChannel.invertedIn
        outputStream = clientChannel.invertedOut
        errorStream = clientChannel.invertedErr

        log.debug("init - End")
    }

    fun processFlow(): CmdStrIOE {  // TODO: this will return command execution history
        log.debug("processFlow() - Begin")

        log.info("Processing flow in SSH client shell")

        var outputString = ""
        var errorString = ""

        runBlocking {

            launch(Dispatchers.IO) {
                val lines = command.input.split(endOfLine)
                var i = 0
                for (line in lines) {
                    log.debug("in: $line")
                    inputStream.write(line.toByteArray(charset))
                    listener.onEvent(InputLineEvent(line))
                    i++
                    if (i == lines.size) {
                        break;
                    }
                }
                //inputStream.write(command.input.toByteArray())
                inputStream.flush()
            }

            launch(Dispatchers.IO) {
                outputStream.reader(Charsets.UTF_8).use {
                    reader ->
                        reader.forEachLine {
                            line ->
                                log.debug("out: $line")
                                listener.onEvent(OutputLineEvent(line))
                                outputString = outputString + line + endOfLine
                        }
                }
            }

            launch(Dispatchers.IO) {
                errorStream.reader(Charsets.UTF_8).use {
                    reader ->
                        reader.forEachLine {
                                line ->
                                    log.debug("err: $line")
                                    listener.onEvent(ErrorLineEvent(line))
                                    errorString = errorString + line + endOfLine
                        }
                }
            }

        }

        val result = command.copy(output = outputString, error = errorString)

        log.debug("processFlow() - End, Return result=$result")

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

    companion object {
        private val log = LoggerFactory.getLogger(SshClientShell::class.java)
    }
}
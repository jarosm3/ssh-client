package org.cameek.sshclient.stream

import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannel
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.channel.Channel
import org.cameek.sshclient.bean.CmdStrIOE
import org.cameek.sshclient.event.EmptyListener
import org.cameek.sshclient.event.Listener
import org.cameek.sshclient.service.SshClientService
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class SshClientShell(

    val host: String,
    val port: Int,
    val timeoutMillis: Long = 1000,
    val username: String,
    val password: String,
    val commands: Iterable<CmdStrIOE>,  // TODO: here should be CommandProvider (SequentialCmdProvider, FlowCmdProvider)
    val listener: Listener = EmptyListener()

): AutoCloseable {

    private val client: SshClient
    private val clientSession: ClientSession
    private val clientChannel: ClientChannel
    private val outputStream: ByteArrayOutputStream
    private val errorStream: ByteArrayOutputStream
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

        outputStream = ByteArrayOutputStream()
        errorStream = ByteArrayOutputStream()

        clientChannel = clientSession.createChannel(Channel.CHANNEL_SHELL)
        clientChannel.open().verify(timeoutMillis, TimeUnit.MILLISECONDS);
        clientChannel.setOut(outputStream)
        clientChannel.setErr(errorStream)

        inputStream = clientChannel.invertedIn

        log.debug("init - End")
    }

    fun processFlow(): Iterable<CmdStrIOE> {  // TODO: this will return command execution history
        log.debug("processFlow() - Begin")

        log.info("Processing flow in SSH client shell")

        val result = emptyList<CmdStrIOE>()

        log.debug("processFlow() - End")

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
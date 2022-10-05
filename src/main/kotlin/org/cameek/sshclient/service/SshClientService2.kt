package org.cameek.sshclient.service

import org.apache.sshd.client.SshClient
import org.apache.sshd.client.channel.ClientChannelEvent
import org.apache.sshd.common.channel.Channel
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class SshClientService2 {
    @Throws(IOException::class)
    fun listFolderStructure(
        username: String?, password: String?,
        host: String?, port: Int, defaultTimeoutSeconds: Long, command: String
    ) {
        val client = SshClient.setUpDefaultClient()
        client.start()
        try {
            client.connect(username, host, port)
                .verify(defaultTimeoutSeconds, TimeUnit.SECONDS).session.use { session ->
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
                                    EnumSet.of(ClientChannelEvent.CLOSED),
                                    TimeUnit.SECONDS.toMillis(defaultTimeoutSeconds)
                                )
                                val responseString = String(responseStream.toByteArray())
                                println(responseString)
                            } finally {
                                channel.close(false)
                            }
                        }
                    }
                }
        } finally {
            client.stop()
        }
    }
}
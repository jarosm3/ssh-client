package org.cameek.sshclient.stream

import org.cameek.sshclient.bean.CmdStrIOE
import org.cameek.sshclient.event.EmptyListener
import org.cameek.sshclient.event.Listener

class SshClientShell(

    val host: String, val port: Int, val timeoutMillis: Long,
    val username: String, val password: String,
    val commands: Iterable<CmdStrIOE>,  // TODO: here should be CommandProvider (SequentialCmdProvider, FlowCmdProvider)
    val listener: Listener = EmptyListener()

): AutoCloseable {

    fun open() {

    }

    fun process(): Iterable<CmdStrIOE> {  // TODO: this will return command execution history
        return emptyList()
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}
package org.cameek.sshclient.bean

data class CmdIOE(
    val input: String = "",
    val output: String = "",
    val error: String = ""
) {
}
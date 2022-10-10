package org.cameek.sshclient.bean

data class CmdStrIOE(
    val input: String = "",
    val output: String = "",
    val error: String = "",
    val state: String = "NEW"   // TODO: define some state enum (NEW, COMPLETED, ERROR)
)
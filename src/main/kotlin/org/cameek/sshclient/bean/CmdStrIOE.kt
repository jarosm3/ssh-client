package org.cameek.sshclient.bean

import org.apache.commons.text.StringEscapeUtils

data class CmdStrIOE(
    val input: String = "",
    val output: String = "",
    val error: String = "",
    val state: String = "NEW"   // TODO: define some state enum (NEW, COMPLETED, ERROR)
) {
    override fun toString(): String {
        val printInput = StringEscapeUtils.escapeJava(input)
        val printOutput = StringEscapeUtils.escapeJava(output)
        val printError = StringEscapeUtils.escapeJava(error)
        return "CmdStrIOE(input='$printInput', output='$printOutput', error='$printError', state='$state')"
    }
}
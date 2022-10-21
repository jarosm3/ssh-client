package org.cameek.sshclient.event

data class ErrorLineEvent(
    override val line: String
) : LineEvent
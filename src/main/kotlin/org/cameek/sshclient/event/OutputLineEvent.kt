package org.cameek.sshclient.event

data class OutputLineEvent(
    override val line: String
) : LineEvent
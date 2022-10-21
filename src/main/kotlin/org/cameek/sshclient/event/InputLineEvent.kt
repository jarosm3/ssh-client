package org.cameek.sshclient.event

data class InputLineEvent(
    override val line: String
) : LineEvent
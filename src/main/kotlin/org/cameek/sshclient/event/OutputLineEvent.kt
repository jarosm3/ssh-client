package org.cameek.sshclient.event

data class OutputLineEvent(
        val line: String
    ) : Event {
}
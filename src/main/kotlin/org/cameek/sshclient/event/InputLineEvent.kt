package org.cameek.sshclient.event

data class InputLineEvent(
        val line: String
    ) : Event {
}
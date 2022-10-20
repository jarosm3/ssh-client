package org.cameek.sshclient.event

data class ErrorLineEvent(
        val line: String
    ) : Event {
}
package org.cameek.sshclient.event

interface LineEvent : Event {
    val line: String
}
package org.cameek.sshclient.event

interface Listener {
    fun onEvent(event: Event)
}
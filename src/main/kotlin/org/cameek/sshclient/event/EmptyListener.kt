package org.cameek.sshclient.event

import org.slf4j.LoggerFactory

class EmptyListener : Listener {

    companion object {
        private val log = LoggerFactory.getLogger(EmptyListener::class.java)
        val singleton = EmptyListener()
    }

    override fun onEvent(event: Event) {
        log.debug("onEvent(...), event=$event")
    }

}
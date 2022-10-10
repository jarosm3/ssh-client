package org.cameek.sshclient.stream

import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber

class StreamIOE(
    val input: Subscriber<String>,
    val output: Publisher<String>,
    val error: Publisher<String>
) {
}
package org.cameek.sshclient.service.filter

data class ProcessingContext(
    val currentLine: String = "",
    val inputLines: List<String> = emptyList(),
    val inputLineIndex: Int = 0
)
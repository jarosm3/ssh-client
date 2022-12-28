package org.cameek.sshclient.service.filter

import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import java.util.function.Predicate
import java.util.regex.Pattern

class BasicSshFilter : Predicate<ProcessingContext> {
    companion object {
        val singleton = BasicSshFilter()
        private val log = LoggerFactory.getLogger(BasicSshFilter::class.java)
        private val ignorePatterns = arrayOf(Pattern.compile("tput[:] unknown terminal [\"]dummy[\"](\r?)(\n?)"))
    }

    override fun test(processingContext: ProcessingContext): Boolean {

        var result = true

        if (log.isDebugEnabled) {
            val printLine = StringEscapeUtils.escapeJava(processingContext.currentLine)
            log.debug("test(line=$printLine) - Begin")
        }

        for (ignorePattern in ignorePatterns) {

            if (ignorePattern.matcher(processingContext.currentLine).matches()) {

                if (log.isDebugEnabled) {
                    val printPattern = StringEscapeUtils.escapeJava(ignorePattern.pattern())
                    val printLine = StringEscapeUtils.escapeJava(processingContext.currentLine)
                    log.debug("Ignore pattern=$printPattern matches the line=$printLine")
                }

                result = false

                break;
            }

        }

        if (log.isDebugEnabled) {
            val printLine = StringEscapeUtils.escapeJava(processingContext.currentLine)
            log.debug("test(line=$printLine) - End, Return result=$result")
        }

        return  result
    }
}
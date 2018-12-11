package net.jcflorezr.exception

import com.fasterxml.jackson.annotation.JsonInclude
import java.lang.RuntimeException
import kotlin.collections.LinkedHashMap

open class AudioSplitterCustomException : RuntimeException()

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class InternalServerErrorException(
    e: Exception
) : AudioSplitterCustomException() {

    private val exceptionClassName: String = e.javaClass.name
    override val message: String? = e.message
    private val simplifiedStackTrace: List<SimplifiedStackTraceElement>

    init {
        simplifiedStackTrace = getSimplifiedStackTrace(e.stackTrace)
    }

    private fun getSimplifiedStackTrace(
        stackTraceElements: Array<StackTraceElement>
    ) = stackTraceElements.asSequence().groupByTo(
            destination = LinkedHashMap(),
            keySelector = {it.className},
            valueTransform = {it.lineNumber}
        ).map {SimplifiedStackTraceElement(it.key, it.value)}

    data class SimplifiedStackTraceElement(
        val className: String,
        val lines: List<Int>
    )

}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
open class BadRequestException(
    override val message: String,
    private val suggestion: String?
) : AudioSplitterCustomException() {

    override fun toString(): String {
        return "audioSplitterCustomException{" +
                "message='" + message + '\''.toString() +
                ", suggestion='" + suggestion + '\''.toString() +
                '}'.toString()
    }
}
package net.jcflorezr.exception

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import net.jcflorezr.broker.Message
import java.lang.RuntimeException
import kotlin.collections.LinkedHashMap

@JsonIgnoreProperties(value = ["stackTrace", "localizedMessage"])
open class AudioSplitterException(val errorCode: String) : RuntimeException(), Message

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class InternalServerErrorException(
    errorCode: String = "outer_error",
    val ex: Exception
) : AudioSplitterException(errorCode) {

    override val message: String? = ex.message ?: ex.localizedMessage
    private val simplifiedStackTrace: List<SimplifiedStackTraceElement>

    init {
        simplifiedStackTrace = generateSimplifiedStackTrace(ex.stackTrace)
    }

    private fun generateSimplifiedStackTrace(
        stackTraceElements: Array<StackTraceElement>
    ) = stackTraceElements.asSequence().groupByTo(
            destination = LinkedHashMap(),
            keySelector = { it.className },
            valueTransform = { it.lineNumber }
        ).map { SimplifiedStackTraceElement(it.key, it.value) }

    fun getSimplifiedStackTrace() = simplifiedStackTrace

    override fun toString(): String {
        return "InternalServerErrorException(ex=$ex, message=$message, simplifiedStackTrace=$simplifiedStackTrace)"
    }

    data class SimplifiedStackTraceElement(
        val className: String,
        val lines: List<Int>
    )
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
open class BadRequestException(
    errorCode: String,
    override val message: String,
    val suggestion: String?
) : AudioSplitterException(errorCode) {
    override fun toString(): String {
        return "BadRequestException{" +
                "errorCode='" + errorCode + '\''.toString() +
                ", message='" + message + '\''.toString() +
                ", suggestion='" + suggestion + '\''.toString() +
            '}'.toString()
    }
}
package net.jcflorezr.transcriber.core.exception

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.util.StringJoiner
import kotlin.collections.LinkedHashMap

@JsonIgnoreProperties(value = ["stackTrace", "localizedMessage"])
open class TranscriberException(val errorCode: String) : RuntimeException()

@JsonInclude(JsonInclude.Include.NON_EMPTY)
open class InternalServerErrorException(
    errorCode: String = "internal_error",
    val throwable: Throwable
) : TranscriberException(errorCode) {

    override val message: String? = throwable.message ?: throwable.localizedMessage
    val simplifiedStackTrace: List<SimplifiedStackTraceElement>

    init {
        simplifiedStackTrace = generateSimplifiedStackTrace(throwable.stackTrace)
    }

    private fun generateSimplifiedStackTrace(
        stackTraceElements: Array<StackTraceElement>
    ) = stackTraceElements.asSequence().groupByTo(
        destination = LinkedHashMap(),
        keySelector = { it.className },
        valueTransform = { it.lineNumber }
    ).map { SimplifiedStackTraceElement(it.key, it.value) }

    override fun toString(): String {
        return "InternalServerErrorException(ex=$throwable, message=$message, simplifiedStackTrace=$simplifiedStackTrace)"
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
) : TranscriberException(errorCode) {

    override fun toString() =
        StringJoiner(", ", BadRequestException::class.java.simpleName + "[", "]")
            .add("errorCode='$errorCode'")
            .add("message=$message")
            .add("suggestion=$suggestion")
            .toString()
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
open class NotFoundException(
    errorCode: String,
    override val message: String
) : TranscriberException(errorCode) {

    override fun toString() =
        StringJoiner(", ", NotFoundException::class.java.simpleName + "[", "]")
            .add("errorCode='$errorCode'")
            .add("message=$message")
            .toString()
}

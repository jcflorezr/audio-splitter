package net.jcflorezr.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import mu.KotlinLogging
import net.jcflorezr.exception.AudioSplitterException
import net.jcflorezr.exception.BadRequestException

interface AudioSplitterResponse

data class SuccessResponse(
    val message: String,
    val transactionId: String
) : AudioSplitterResponse

class ErrorResponseSerializer @JvmOverloads constructor(t: Class<ErrorResponse>? = null) : StdSerializer<ErrorResponse>(t) {

    private val logger = KotlinLogging.logger { }

    override fun serialize(errorResponse: ErrorResponse, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
        jsonGenerator.writeStartObject()
        if (errorResponse.audioSplitterException is BadRequestException) {
            jsonGenerator.writeObjectField("error", errorResponse.audioSplitterException)
        } else {
            jsonGenerator.writeStringField("message", "Please try again later.")
        }
        jsonGenerator.writeEndObject()
    }
}

@JsonSerialize(using = ErrorResponseSerializer::class)
data class ErrorResponse(
    val audioSplitterException: AudioSplitterException
) : AudioSplitterResponse
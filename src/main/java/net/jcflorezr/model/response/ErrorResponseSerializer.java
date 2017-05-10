package net.jcflorezr.model.response;

import net.jcflorezr.exceptions.AudioSplitterCustomException;
import net.jcflorezr.exceptions.BadRequestException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ErrorResponseSerializer extends StdSerializer<ErrorResponse> {

    public ErrorResponseSerializer() {
        this(null);
    }

    protected ErrorResponseSerializer(Class<ErrorResponse> t) {
        super(t);
    }

    @Override
    public void serialize(ErrorResponse errorResponse, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        AudioSplitterCustomException audioSplitterCustomException = errorResponse.getAudioSplitterCustomException();
        jsonGenerator.writeStartObject();
        if (audioSplitterCustomException instanceof BadRequestException) {
            jsonGenerator.writeObjectField("error", audioSplitterCustomException);
        } else {
            jsonGenerator.writeStringField("message", "Please try again later.");
        }
        jsonGenerator.writeEndObject();
    }
}

package net.jcflorezr.model.response;

import net.jcflorezr.api.model.response.AudioSplitterResponse;
import net.jcflorezr.exceptions.AudioSplitterCustomException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ErrorResponseSerializer.class)
public class ErrorResponse implements AudioSplitterResponse {

    AudioSplitterCustomException audioSplitterCustomException;

    public ErrorResponse(AudioSplitterCustomException audioSplitterCustomException) {
        this.audioSplitterCustomException = audioSplitterCustomException;
    }

    public AudioSplitterCustomException getAudioSplitterCustomException() {
        return audioSplitterCustomException;
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "audioSplitterCustomException=" + audioSplitterCustomException +
                '}';
    }
}

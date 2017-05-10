package net.jcflorezr.model.response;

import net.jcflorezr.exceptions.AudioSplitterCustomException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = ErrorResponseSerializer.class)
public class ErrorResponse implements AudioSplitterResponse {

    private AudioSplitterCustomException audioSplitterCustomException;

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

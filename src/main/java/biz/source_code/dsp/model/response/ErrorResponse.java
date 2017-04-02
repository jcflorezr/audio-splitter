package biz.source_code.dsp.model.response;

import biz.source_code.dsp.api.model.response.AudioSplitterResponse;
import biz.source_code.dsp.exceptions.AudioSplitterCustomException;
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

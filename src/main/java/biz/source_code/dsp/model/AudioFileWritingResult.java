package biz.source_code.dsp.model;

import biz.source_code.dsp.exceptions.InternalServerErrorException;

public class AudioFileWritingResult {

    private boolean success;
    private InternalServerErrorException exception;

    private AudioFileWritingResult(boolean success) {
        this.success = success;
    }

    private AudioFileWritingResult(boolean success, InternalServerErrorException exception) {
        this.success = success;
        this.exception = exception;
    }

    public static AudioFileWritingResult successful() {
        return new AudioFileWritingResult(true);
    }

    public static AudioFileWritingResult unsuccessful(Exception e) {
        return new AudioFileWritingResult(false, new InternalServerErrorException(e));
    }

    public boolean isSuccess() {
        return success;
    }

    public InternalServerErrorException getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "AudioFileWritingResult{" +
                "success=" + success +
                ", exception=" + exception +
                '}';
    }
}

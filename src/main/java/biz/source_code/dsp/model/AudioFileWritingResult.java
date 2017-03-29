package biz.source_code.dsp.model;

public class AudioFileWritingResult {

    private boolean success;
    private String message;

    public AudioFileWritingResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}

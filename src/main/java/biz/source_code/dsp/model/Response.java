package biz.source_code.dsp.model;

public class Response {

    private boolean success;
    private int samplingRate;
    private String message;

    public Response(boolean success, int samplingRate, String message) {
        this.success = success;
        this.samplingRate = samplingRate;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public String getMessage() {
        return message;
    }
}

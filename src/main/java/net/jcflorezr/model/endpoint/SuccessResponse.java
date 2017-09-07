package net.jcflorezr.model.endpoint;

public class SuccessResponse implements AudioSplitterResponse {

    private Long numOfSuccessAudioClips;
    private Long numOfFailedAudioClips;

    public SuccessResponse(Long numOfSuccessAudioClips, Long numOfFailedAudioClips) {
        this.numOfSuccessAudioClips = numOfSuccessAudioClips;
        this.numOfFailedAudioClips = numOfFailedAudioClips;
    }

    public Long getNumOfSuccessAudioClips() {
        return numOfSuccessAudioClips;
    }

    public Long getNumOfFailedAudioClips() {
        return numOfFailedAudioClips;
    }

    @Override
    public String toString() {
        return "SuccessResponse{" +
                "numOfSuccessAudioClips=" + numOfSuccessAudioClips +
                ", numOfFailedAudioClips=" + numOfFailedAudioClips +
                '}';
    }
}

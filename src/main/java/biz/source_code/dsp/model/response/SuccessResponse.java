package biz.source_code.dsp.model.response;

import biz.source_code.dsp.api.model.response.AudioSplitterResponse;

public class SuccessResponse implements AudioSplitterResponse {

    private Long numOfSuccessSoundZones;
    private Long numOfFailedSoundZones;

    public SuccessResponse(Long numOfSuccessSoundZones, Long numOfFailedSoundZones) {
        this.numOfSuccessSoundZones = numOfSuccessSoundZones;
        this.numOfFailedSoundZones = numOfFailedSoundZones;
    }

    public Long getNumOfSuccessSoundZones() {
        return numOfSuccessSoundZones;
    }

    public Long getNumOfFailedSoundZones() {
        return numOfFailedSoundZones;
    }

    @Override
    public String toString() {
        return "SuccessResponse{" +
                "numOfSuccessSoundZones=" + numOfSuccessSoundZones +
                ", numOfFailedSoundZones=" + numOfFailedSoundZones +
                '}';
    }
}

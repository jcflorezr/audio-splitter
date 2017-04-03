package net.jcflorezr.api.model;

import biz.source_code.dsp.model.AudioFileWritingResult;

public interface AudioSoundZoneInfo {

    String getSuggestedAudioFileName();

    float getStartPositionInSeconds();

    float getDurationInSeconds();

    int getHours();

    int getMinutes();

    int getSeconds();

    int getMilliseconds();

    AudioFileWritingResult getAudioClipWritingResult();

    void setAudioClipWritingResult(AudioFileWritingResult audioClipWritingResult);
}

package net.jcflorezr.model.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;

public interface AudioClipInfo {

    String getSuggestedAudioClipName();

    float getStartPositionInSeconds();

    float getDurationInSeconds();

    int getHours();

    int getMinutes();

    int getSeconds();

    int getMilliseconds();

    AudioFileWritingResult getAudioClipWritingResult();

    void setAudioClipWritingResult(AudioFileWritingResult audioClipWritingResult);
}

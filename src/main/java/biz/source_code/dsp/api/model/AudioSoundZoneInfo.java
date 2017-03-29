package biz.source_code.dsp.api.model;

public interface AudioSoundZoneInfo {

    String getSuggestedAudioFileName();

    float getStartPositionInSeconds();

    float getDurationInSeconds();

    int getHours();

    int getMinutes();

    int getSeconds();

    int getMilliseconds();
}

package net.jcflorezr.model.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;

public class AudioClipsWritingResult {

    private int hours;
    private int minutes;
    private int seconds;
    private int milliseconds;
    private String audioClipName;
    private AudioFileWritingResult audioClipWritingResult;

    public AudioClipsWritingResult(AudioClipInfo audioClipInfo, AudioFileWritingResult audioClipWritingResult, String audioFileNameAndPath) {
        this.hours = audioClipInfo.getHours();
        this.minutes = audioClipInfo.getMinutes();
        this.seconds = audioClipInfo.getSeconds();
        this.milliseconds = audioClipInfo.getMilliseconds();
        this.audioClipName = audioFileNameAndPath;
        this.audioClipWritingResult = audioClipWritingResult;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public String getAudioClipName() {
        return audioClipName;
    }

    public AudioFileWritingResult getAudioClipWritingResult() {
        return audioClipWritingResult;
    }

    @Override
    public String toString() {
        return "AudioClipsWritingResult{" +
                "hours=" + hours +
                ", minutes=" + minutes +
                ", seconds=" + seconds +
                ", milliseconds=" + milliseconds +
                ", audioClipName='" + audioClipName + '\'' +
                ", audioClipWritingResult=" + audioClipWritingResult +
                '}';
    }
}

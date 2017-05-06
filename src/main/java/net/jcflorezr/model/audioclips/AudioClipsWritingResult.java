package net.jcflorezr.model.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;

public class AudioClipsWritingResult {

    private String audioClipName;
    private AudioFileWritingResult audioClipWritingResult;

    public AudioClipsWritingResult() {
    }

    public AudioClipsWritingResult(String audioClipName, AudioFileWritingResult audioClipWritingResult) {
        this.audioClipName = audioClipName;
        this.audioClipWritingResult = audioClipWritingResult;
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
                "audioClipName='" + audioClipName + '\'' +
                ", audioClipWritingResult=" + audioClipWritingResult +
                '}';
    }
}

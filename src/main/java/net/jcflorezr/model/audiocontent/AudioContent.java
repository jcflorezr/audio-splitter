package net.jcflorezr.model.audiocontent;

import biz.source_code.dsp.model.AudioSignal;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AudioContent {

    private AudioMetadata audioMetadata;
    private AudioSignal originalAudioSignal;

    public AudioContent(AudioSignal originalAudioSignal, AudioMetadata audioMetadata) {
        this.originalAudioSignal = originalAudioSignal;
        this.audioMetadata = audioMetadata;
    }

    @JsonIgnore
    public AudioSignal getOriginalAudioSignal() {
        return originalAudioSignal;
    }

    @JsonIgnore
    public float[][] getOriginalAudioData() {
        return originalAudioSignal.getData();
    }

    @JsonIgnore
    public int getOriginalAudioSamplingRate() {
        return originalAudioSignal.getSamplingRate();
    }

    @JsonIgnore
    public int getOriginalAudioChannels() {
        return originalAudioSignal.getChannels();
    }

    @JsonIgnore
    public float getOriginalAudioDurationInSeconds() {
        return originalAudioSignal.getLength() / originalAudioSignal.getSamplingRate();
    }

    public AudioMetadata getAudioMetadata() {
        return audioMetadata;
    }
}

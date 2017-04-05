package net.jcflorezr.model.audiocontent;

import biz.source_code.dsp.model.AudioSignal;

public class AudioContent {

    private AudioSignal originalAudioSignal;
    private AudioMetadata audioMetadata;

    public AudioSignal getOriginalAudioSignal() {
        return originalAudioSignal;
    }

    public void setOriginalAudioSignal(AudioSignal originalAudioSignal) {
        this.originalAudioSignal = originalAudioSignal;
    }

    public float[][] getOriginalAudioData() {
        return originalAudioSignal.getData();
    }

    public int getOriginalAudioSamplingRate() {
        return originalAudioSignal.getSamplingRate();
    }

    public int getOriginalAudioChannels() {
        return originalAudioSignal.getChannels();
    }

    public AudioMetadata getAudioMetadata() {
        return audioMetadata;
    }

    public void setAudioMetadata(AudioMetadata audioMetadata) {
        this.audioMetadata = audioMetadata;
    }

    public float getOriginalAudioDurationInSeconds() {
        return originalAudioSignal.getLength() / originalAudioSignal.getSamplingRate();
    }
}

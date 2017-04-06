package net.jcflorezr.model.audiocontent;

import biz.source_code.dsp.model.AudioSignal;

public class AudioContent {

    private AudioSignal originalAudioSignal;
    private AudioMetadata audioMetadata;

    public AudioContent(AudioSignal originalAudioSignal, AudioMetadata audioMetadata) {
        this.originalAudioSignal = originalAudioSignal;
        this.audioMetadata = audioMetadata;
    }

    public AudioSignal getOriginalAudioSignal() {
        return originalAudioSignal;
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

    public float getOriginalAudioDurationInSeconds() {
        return originalAudioSignal.getLength() / originalAudioSignal.getSamplingRate();
    }
}

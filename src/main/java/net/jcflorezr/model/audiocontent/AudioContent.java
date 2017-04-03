package net.jcflorezr.model.audiocontent;

import biz.source_code.dsp.model.AudioSignal;

public class AudioContent {

    private AudioSignal originalAudioSignal;
    private AudioSignal separatorAudioSignal;
    private AudioMetadata audioMetadata;

    public AudioSignal getOriginalAudioSignal() {
        return originalAudioSignal;
    }

    public void setOriginalAudioSignal(AudioSignal originalAudioSignal) {
        this.originalAudioSignal = originalAudioSignal;
    }

    public void setSeparatorAudioSignal(AudioSignal separatorAudioSignal) {
        this.separatorAudioSignal = separatorAudioSignal;
    }

    public AudioSignal getSeparatorAudioSignal() {
        return separatorAudioSignal;
    }

    public float[][] getOriginalAudioData() {
        return originalAudioSignal.getData();
    }

    public float[][] getSeparatorAudioData() {
        return separatorAudioSignal.getData();
    }

    public int getOriginalAudioSamplingRate() {
        return originalAudioSignal.getSamplingRate();
    }

    public int getSeparatorAudioSamplingRate() {
        return separatorAudioSignal.getSamplingRate();
    }

    public int getOriginalAudioChannels() {
        return originalAudioSignal.getChannels();
    }

    public int getSeparatorAudioChannels() {
        return separatorAudioSignal.getChannels();
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

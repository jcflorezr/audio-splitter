package net.jcflorezr.model.audioclips;

import net.jcflorezr.model.audiocontent.AudioContent;

public class OutputAudioClipsConfig {

    private String outputAudioClipsDirectoryPath;
    private AudioContent audioContent;
    private String audioFormatExtension;
    private boolean mono;
    private boolean withSeparator;

    public OutputAudioClipsConfig(String outputAudioClipsDirectoryPath, AudioContent audioContent, String audioFormatExtension, boolean mono, boolean withSeparator) {
        this.outputAudioClipsDirectoryPath = outputAudioClipsDirectoryPath;
        this.audioContent = audioContent;
        this.audioFormatExtension = audioFormatExtension;
        this.mono = mono;
        this.withSeparator = withSeparator;
    }

    public String getOutputAudioClipsDirectoryPath() {
        return outputAudioClipsDirectoryPath;
    }

    public AudioContent getAudioContent() {
        return audioContent;
    }

    public String getAudioFormatExtension() {
        return audioFormatExtension;
    }

    public boolean isMono() {
        return mono;
    }

    public boolean isWithSeparator() {
        return withSeparator;
    }
}

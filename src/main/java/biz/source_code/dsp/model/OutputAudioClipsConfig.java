package biz.source_code.dsp.model;

public class OutputAudioClipsConfig {

    private String outputAudioClipsDirectoryPath;
    private AudioContent audioContent;
    private String audioFormatExtension;
    private boolean mono;

    public OutputAudioClipsConfig(String outputAudioClipsDirectoryPath, AudioContent audioContent, String audioFormatExtension, boolean mono) {
        this.outputAudioClipsDirectoryPath = outputAudioClipsDirectoryPath;
        this.audioContent = audioContent;
        this.audioFormatExtension = audioFormatExtension;
        this.mono = mono;
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
}

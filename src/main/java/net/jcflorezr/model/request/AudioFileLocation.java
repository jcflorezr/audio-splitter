package net.jcflorezr.model.request;

public class AudioFileLocation {

    private String audioFileName;
    private String outputAudioClipsDirectoryPath;

    public AudioFileLocation() {
    }

    public AudioFileLocation(String audioFileName, String outputAudioClipsDirectoryPath) {
        this.audioFileName = audioFileName;
        this.outputAudioClipsDirectoryPath = outputAudioClipsDirectoryPath;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public String getOutputAudioClipsDirectoryPath() {
        return outputAudioClipsDirectoryPath;
    }
}

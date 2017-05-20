package net.jcflorezr.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AudioFileLocation {

    private String audioFileName;
    private String outputAudioClipsDirectoryPath;
    private String convertedAudioFileName;

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

    public String getConvertedAudioFileName() {
        return convertedAudioFileName;
    }

    public void setConvertedAudioFileName(String convertedAudioFileName) {
        this.convertedAudioFileName = convertedAudioFileName;
    }

    @JsonIgnore
    public boolean audioFileWasConverted() {
        return audioFileName.equals(convertedAudioFileName);
    }
}

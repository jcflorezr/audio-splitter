package net.jcflorezr.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

public class AudioFileLocation {

    private String audioFileName;
    private String outputAudioClipsDirectoryPath;
    private String convertedAudioFileName;

    public AudioFileLocation() {
    }

    public AudioFileLocation(String audioFileName, String outputAudioClipsDirectoryPath, String convertedAudioFileName) {
        this.audioFileName = audioFileName;
        this.outputAudioClipsDirectoryPath = outputAudioClipsDirectoryPath;
        this.convertedAudioFileName = convertedAudioFileName;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    @JsonSetter
    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public String getOutputAudioClipsDirectoryPath() {
        return outputAudioClipsDirectoryPath;
    }

    @JsonSetter
    public void setOutputAudioClipsDirectoryPath(String outputAudioClipsDirectoryPath) {
        this.outputAudioClipsDirectoryPath = outputAudioClipsDirectoryPath;
    }

    public String getConvertedAudioFileName() {
        return convertedAudioFileName;
    }

    @JsonSetter
    public void setConvertedAudioFileName(String convertedAudioFileName) {
        this.convertedAudioFileName = convertedAudioFileName;
    }

    @JsonIgnore
    public boolean audioFileWasConverted() {
        return !audioFileName.equals(convertedAudioFileName);
    }
}

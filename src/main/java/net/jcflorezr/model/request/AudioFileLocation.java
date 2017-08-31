package net.jcflorezr.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "audio_file_info")
public class AudioFileLocation {

    @PrimaryKey(value = "audio_file_name")
    private String audioFileName;
    @Column("audio_clips_output_directory")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioFileLocation that = (AudioFileLocation) o;

        if (audioFileName != null ? !audioFileName.equals(that.audioFileName) : that.audioFileName != null)
            return false;
        return outputAudioClipsDirectoryPath != null ? outputAudioClipsDirectoryPath.equals(that.outputAudioClipsDirectoryPath) : that.outputAudioClipsDirectoryPath == null;
    }

    @Override
    public int hashCode() {
        int result = audioFileName != null ? audioFileName.hashCode() : 0;
        result = 31 * result + (outputAudioClipsDirectoryPath != null ? outputAudioClipsDirectoryPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AudioFileLocation{" +
                "audioFileName='" + audioFileName + '\'' +
                ", outputAudioClipsDirectoryPath='" + outputAudioClipsDirectoryPath + '\'' +
                ", convertedAudioFileName='" + convertedAudioFileName + '\'' +
                '}';
    }
}

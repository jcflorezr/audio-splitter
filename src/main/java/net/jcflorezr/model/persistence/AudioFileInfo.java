package net.jcflorezr.model.persistence;

import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "audio_file_info")
public class AudioFileInfo {

    @PrimaryKey("audio_file_name")
    private String audioFileName;
    private String audioClipsOutputDirectory;

    public String getAudioFileName() {
        return audioFileName;
    }

    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public String getAudioClipsOutputDirectory() {
        return audioClipsOutputDirectory;
    }

    public void setAudioClipsOutputDirectory(String audioClipsOutputDirectory) {
        this.audioClipsOutputDirectory = audioClipsOutputDirectory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioFileInfo that = (AudioFileInfo) o;

        if (!audioFileName.equals(that.audioFileName)) return false;
        return audioClipsOutputDirectory.equals(that.audioClipsOutputDirectory);
    }

    @Override
    public int hashCode() {
        int result = audioFileName.hashCode();
        result = 31 * result + audioClipsOutputDirectory.hashCode();
        return result;
    }
}

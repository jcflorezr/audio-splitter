package net.jcflorezr.model.persistence;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class AudioFileNamePrimaryKey {

    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String audioFileName;

    public AudioFileNamePrimaryKey() {
    }

    public AudioFileNamePrimaryKey(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioFileNamePrimaryKey that = (AudioFileNamePrimaryKey) o;

        return audioFileName != null ? audioFileName.equals(that.audioFileName) : that.audioFileName == null;
    }

    @Override
    public int hashCode() {
        return audioFileName != null ? audioFileName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AudioFileNamePrimaryKey{" +
                "audioFileName='" + audioFileName + '\'' +
                '}';
    }
}

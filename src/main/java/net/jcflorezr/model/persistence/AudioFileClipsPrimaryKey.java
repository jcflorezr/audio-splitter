package net.jcflorezr.model.persistence;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

import java.io.Serializable;

@PrimaryKeyClass
public class AudioFileClipsPrimaryKey implements Serializable {

    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String audioFileName;
    @PrimaryKeyColumn(name = "hours", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private int hours;
    @PrimaryKeyColumn(name = "minutes", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private int minutes;
    @PrimaryKeyColumn(name = "seconds", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    private int seconds;
    @PrimaryKeyColumn(name = "milliseconds", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    private int milliseconds;
    @PrimaryKeyColumn(name = "audio_clip_name", ordinal = 5, type = PrimaryKeyType.CLUSTERED)
    private String audioClipName;

    public AudioFileClipsPrimaryKey() {
    }

    private AudioFileClipsPrimaryKey(String audioFileName, int hours, int minutes, int seconds, int milliseconds, String audioClipName) {
        this.audioFileName = audioFileName;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
        this.audioClipName = audioClipName;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public String getAudioClipName() {
        return audioClipName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioFileClipsPrimaryKey that = (AudioFileClipsPrimaryKey) o;

        if (hours != that.hours) return false;
        if (minutes != that.minutes) return false;
        if (seconds != that.seconds) return false;
        if (milliseconds != that.milliseconds) return false;
        if (audioFileName != null ? !audioFileName.equals(that.audioFileName) : that.audioFileName != null)
            return false;
        return audioClipName != null ? audioClipName.equals(that.audioClipName) : that.audioClipName == null;
    }

    @Override
    public int hashCode() {
        int result = audioFileName != null ? audioFileName.hashCode() : 0;
        result = 31 * result + hours;
        result = 31 * result + minutes;
        result = 31 * result + seconds;
        result = 31 * result + milliseconds;
        result = 31 * result + (audioClipName != null ? audioClipName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AudioFileClipsPrimaryKey{" +
                "audioFileName=" + audioFileName +
                ", hours=" + hours +
                ", minutes=" + minutes +
                ", seconds=" + seconds +
                ", milliseconds=" + milliseconds +
                ", audioClipName='" + audioClipName + '\'' +
                '}';
    }

    public static class AudioClipsPrimaryKeyBuilder {

        private String audioFileName;
        private int hours;
        private int minutes;
        private int seconds;
        private int milliseconds;
        private String audioClipName;

        public AudioClipsPrimaryKeyBuilder audioFileName(String audioFileName) {
            this.audioFileName = audioFileName;
            return this;
        }

        public AudioClipsPrimaryKeyBuilder hours(int hours) {
            this.hours = hours;
            return this;
        }

        public AudioClipsPrimaryKeyBuilder minutes(int minutes) {
            this.minutes = minutes;
            return this;
        }

        public AudioClipsPrimaryKeyBuilder seconds(int seconds) {
            this.seconds = seconds;
            return this;
        }

        public AudioClipsPrimaryKeyBuilder milliseconds(int milliseconds) {
            this.milliseconds = milliseconds;
            return this;
        }

        public AudioClipsPrimaryKeyBuilder audioClipName(String audioClipName) {
            this.audioClipName = audioClipName;
            return this;
        }

        public AudioFileClipsPrimaryKey build() {
            return new AudioFileClipsPrimaryKey(
                    audioFileName,
                    hours,
                    minutes,
                    seconds,
                    milliseconds,
                    audioClipName
            );
        }
    }
}

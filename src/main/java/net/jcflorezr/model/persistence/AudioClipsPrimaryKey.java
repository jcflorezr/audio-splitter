package net.jcflorezr.model.persistence;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class AudioClipsPrimaryKey {

    @PrimaryKey
    private AudioFileNamePrimaryKey audioFileNamePrimaryKey;
    @PrimaryKeyColumn(name = "hours", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private int hours;
    @PrimaryKeyColumn(name = "minute", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private int minutes;
    @PrimaryKeyColumn(name = "seconds", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    private int seconds;
    @PrimaryKeyColumn(name = "milliseconds", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    private int milliseconds;
    @PrimaryKeyColumn(name = "audio_clip_name", ordinal = 5, type = PrimaryKeyType.CLUSTERED)
    private String audioClipName;

    public AudioClipsPrimaryKey() {
    }

    private AudioClipsPrimaryKey(AudioFileNamePrimaryKey audioFileNamePrimaryKey, int hours, int minutes, int seconds, int milliseconds, String audioClipName) {
        this.audioFileNamePrimaryKey = audioFileNamePrimaryKey;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
        this.audioClipName = audioClipName;
    }

    public AudioFileNamePrimaryKey getAudioFileNamePrimaryKey() {
        return audioFileNamePrimaryKey;
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

        AudioClipsPrimaryKey that = (AudioClipsPrimaryKey) o;

        if (hours != that.hours) return false;
        if (minutes != that.minutes) return false;
        if (seconds != that.seconds) return false;
        if (milliseconds != that.milliseconds) return false;
        if (audioFileNamePrimaryKey != null ? !audioFileNamePrimaryKey.equals(that.audioFileNamePrimaryKey) : that.audioFileNamePrimaryKey != null)
            return false;
        return audioClipName != null ? audioClipName.equals(that.audioClipName) : that.audioClipName == null;
    }

    @Override
    public int hashCode() {
        int result = audioFileNamePrimaryKey != null ? audioFileNamePrimaryKey.hashCode() : 0;
        result = 31 * result + hours;
        result = 31 * result + minutes;
        result = 31 * result + seconds;
        result = 31 * result + milliseconds;
        result = 31 * result + (audioClipName != null ? audioClipName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AudioClipsPrimaryKey{" +
                "audioFileNamePrimaryKey=" + audioFileNamePrimaryKey +
                ", hours=" + hours +
                ", minutes=" + minutes +
                ", seconds=" + seconds +
                ", milliseconds=" + milliseconds +
                ", audioClipName='" + audioClipName + '\'' +
                '}';
    }

    public static class AudioClipsPrimaryKeyBuilder {

        private AudioFileNamePrimaryKey audioFileNamePrimaryKey;
        private int hours;
        private int minutes;
        private int seconds;
        private int milliseconds;
        private String audioClipName;

        public AudioClipsPrimaryKeyBuilder audioFileName(String audioFileName) {
            this.audioFileNamePrimaryKey = new AudioFileNamePrimaryKey(audioFileName);
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

        public AudioClipsPrimaryKey build() {
            return new AudioClipsPrimaryKey(
                    audioFileNamePrimaryKey,
                    hours,
                    minutes,
                    seconds,
                    milliseconds,
                    audioClipName
            );
        }
    }
}

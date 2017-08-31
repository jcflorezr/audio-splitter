package net.jcflorezr.model.audioclips;

import net.jcflorezr.model.persistence.AudioClipsPrimaryKey;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "audio_clips")
public class AudioClipInfo {

    @PrimaryKey
    private AudioClipsPrimaryKey audioClipsPrimaryKey;
    @Column("group_number")
    private int groupNumber;
    @Column("start_position")
    private int startPosition;
    @Column("start_position_in_seconds")
    private float startPositionInSeconds;
    @Column("end_position")
    private int endPosition;
    @Column("end_position_in_seconds")
    private float endPositionInSeconds;
    @Column("duration_in_seconds")
    private float durationInSeconds;

    public AudioClipInfo() {
    }

    private AudioClipInfo(String audioFileName, int hours, int minutes, int seconds, int milliseconds, int groupNumber, int startPosition, float startPositionInSeconds, int endPosition, float endPositionInSeconds, float durationInSeconds, String audioClipName) {
        audioClipsPrimaryKey = new AudioClipsPrimaryKey.AudioClipsPrimaryKeyBuilder()
                .audioFileName(audioFileName)
                .hours(hours)
                .minutes(minutes)
                .seconds(seconds)
                .milliseconds(milliseconds)
                .audioClipName(audioClipName)
                .build();
        this.groupNumber = groupNumber;
        this.startPosition = startPosition;
        this.startPositionInSeconds = startPositionInSeconds;
        this.endPosition = endPosition;
        this.endPositionInSeconds = endPositionInSeconds;
        this.durationInSeconds = durationInSeconds;
    }

    public AudioClipsPrimaryKey getAudioFileName() {
        return audioClipsPrimaryKey;
    }

    public int getHours() {
        return audioClipsPrimaryKey.getHours();
    }

    public int getMinutes() {
        return audioClipsPrimaryKey.getMinutes();
    }

    public int getSeconds() {
        return audioClipsPrimaryKey.getSeconds();
    }

    public int getMilliseconds() {
        return audioClipsPrimaryKey.getMilliseconds();
    }

    public String getAudioClipName() {
        return audioClipsPrimaryKey.getAudioClipName();
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public float getStartPositionInSeconds() {
        return startPositionInSeconds;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public float getEndPositionInSeconds() {
        return endPositionInSeconds;
    }

    public float getDurationInSeconds() {
        return durationInSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioClipInfo that = (AudioClipInfo) o;

        return audioClipsPrimaryKey != null ? audioClipsPrimaryKey.equals(that.audioClipsPrimaryKey) : that.audioClipsPrimaryKey == null;
    }

    @Override
    public int hashCode() {
        return audioClipsPrimaryKey != null ? audioClipsPrimaryKey.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AudioClipInfo{" +
                "audioClipsPrimaryKey=" + audioClipsPrimaryKey +
                ", groupNumber=" + groupNumber +
                ", startPosition=" + startPosition +
                ", startPositionInSeconds=" + startPositionInSeconds +
                ", endPosition=" + endPosition +
                ", endPositionInSeconds=" + endPositionInSeconds +
                ", durationInSeconds=" + durationInSeconds +
                '}';
    }

    public static class SingleAudioSoundZoneInfoBuilder {

        private String audioFileName;
        private int hours;
        private int minutes;
        private int seconds;
        private int milliseconds;
        private String suggestedAudioClipName;
        private int groupNumber;
        private int startPosition;
        private float startPositionInSeconds;
        private int endPosition;
        private float endPositionInSeconds;
        private float durationInSeconds;

        public SingleAudioSoundZoneInfoBuilder audioFileName(String audioFileName) {
            this.audioFileName = audioFileName;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder hours(int hours) {
            this.hours = hours;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder minutes(int minutes) {
            this.minutes = minutes;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder seconds(int seconds) {
            this.seconds = seconds;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder milliseconds(int milliseconds) {
            this.milliseconds = milliseconds;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder suggestedAudioClipName(String suggestedAudioClipName) {
            this.suggestedAudioClipName = suggestedAudioClipName;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder groupNumber(int groupNumber) {
            this.groupNumber = groupNumber;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder startPosition(int startPosition) {
            this.startPosition = startPosition;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder startPositionInSeconds(float startPositionInSeconds) {
            this.startPositionInSeconds = startPositionInSeconds;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder endPosition(int endPosition) {
            this.endPosition = endPosition;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder endPositionInSeconds(float endPositionInSeconds) {
            this.endPositionInSeconds = endPositionInSeconds;
            return this;
        }

        public SingleAudioSoundZoneInfoBuilder durationInSeconds(float durationInSeconds) {
            this.durationInSeconds = durationInSeconds;
            return this;
        }

        public AudioClipInfo build() {
            return new AudioClipInfo(
                    audioFileName,
                    hours,
                    minutes,
                    seconds,
                    milliseconds,
                    groupNumber,
                    startPosition,
                    startPositionInSeconds,
                    endPosition,
                    endPositionInSeconds,
                    durationInSeconds,
                    suggestedAudioClipName);
        }
    }
}

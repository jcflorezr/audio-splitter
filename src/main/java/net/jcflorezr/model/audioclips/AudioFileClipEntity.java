package net.jcflorezr.model.audioclips;

import net.jcflorezr.model.persistence.AudioFileClipsPrimaryKey;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table(value = "audio_clips")
public class AudioFileClipEntity {

    @PrimaryKey
    private AudioFileClipsPrimaryKey audioFileClipsPrimaryKey;
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

    public AudioFileClipEntity() {
    }

    private AudioFileClipEntity(String audioFileName, int hours, int minutes, int seconds, int milliseconds, int groupNumber, int startPosition, float startPositionInSeconds, int endPosition, float endPositionInSeconds, float durationInSeconds, String audioClipName) {
        audioFileClipsPrimaryKey = new AudioFileClipsPrimaryKey.AudioClipsPrimaryKeyBuilder()
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

    public AudioFileClipsPrimaryKey getAudioFileClipsPrimaryKey() {
        return audioFileClipsPrimaryKey;
    }

    public int getHours() {
        return audioFileClipsPrimaryKey.getHours();
    }

    public int getMinutes() {
        return audioFileClipsPrimaryKey.getMinutes();
    }

    public int getSeconds() {
        return audioFileClipsPrimaryKey.getSeconds();
    }

    public int getMilliseconds() {
        return audioFileClipsPrimaryKey.getMilliseconds();
    }

    public String getAudioClipName() {
        return audioFileClipsPrimaryKey.getAudioClipName();
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

        AudioFileClipEntity that = (AudioFileClipEntity) o;

        return audioFileClipsPrimaryKey != null ? audioFileClipsPrimaryKey.equals(that.audioFileClipsPrimaryKey) : that.audioFileClipsPrimaryKey == null;
    }

    @Override
    public int hashCode() {
        return audioFileClipsPrimaryKey != null ? audioFileClipsPrimaryKey.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AudioFileClipEntity{" +
                "audioFileClipsPrimaryKey=" + audioFileClipsPrimaryKey +
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

        public AudioFileClipEntity build() {
            return new AudioFileClipEntity(
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

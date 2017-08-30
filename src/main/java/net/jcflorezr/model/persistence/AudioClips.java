package net.jcflorezr.model.persistence;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "audio_clips")
public class AudioClips {

    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String audioFileName;
    @PrimaryKeyColumn(name = "hours", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private int hours;
    @PrimaryKeyColumn(name = "minute", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private int minutes;
    @PrimaryKeyColumn(name = "seconds", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    private int seconds;
    @PrimaryKeyColumn(name = "milliseconds", ordinal = 4, type = PrimaryKeyType.CLUSTERED)

    private int milliseconds;
    private String audioClipName;
    private int groupNumber;
    private int startPosition;
    private float startPositionInSeconds;
    private int endPosition;
    private float endPositionInSeconds;
    private float durationInSeconds;

    public String getAudioFileName() {
        return audioFileName;
    }

    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    public String getAudioClipName() {
        return audioClipName;
    }

    public void setAudioClipName(String audioClipName) {
        this.audioClipName = audioClipName;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public float getStartPositionInSeconds() {
        return startPositionInSeconds;
    }

    public void setStartPositionInSeconds(float startPositionInSeconds) {
        this.startPositionInSeconds = startPositionInSeconds;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public float getEndPositionInSeconds() {
        return endPositionInSeconds;
    }

    public void setEndPositionInSeconds(float endPositionInSeconds) {
        this.endPositionInSeconds = endPositionInSeconds;
    }

    public float getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(float durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioClips that = (AudioClips) o;

        if (hours != that.hours) return false;
        if (minutes != that.minutes) return false;
        if (seconds != that.seconds) return false;
        if (milliseconds != that.milliseconds) return false;
        if (!audioFileName.equals(that.audioFileName)) return false;
        return audioClipName.equals(that.audioClipName);
    }

    @Override
    public int hashCode() {
        int result = audioFileName.hashCode();
        result = 31 * result + hours;
        result = 31 * result + minutes;
        result = 31 * result + seconds;
        result = 31 * result + milliseconds;
        result = 31 * result + audioClipName.hashCode();
        return result;
    }
}

package net.jcflorezr.model.persistence;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value = "audio_clip_result")
public class AudioClipsResult {

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

    private boolean success;
    private String errorMessage;

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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioClipsResult that = (AudioClipsResult) o;

        if (hours != that.hours) return false;
        if (minutes != that.minutes) return false;
        if (seconds != that.seconds) return false;
        if (milliseconds != that.milliseconds) return false;
        if (success != that.success) return false;
        return audioFileName.equals(that.audioFileName);
    }

    @Override
    public int hashCode() {
        int result = audioFileName.hashCode();
        result = 31 * result + hours;
        result = 31 * result + minutes;
        result = 31 * result + seconds;
        result = 31 * result + milliseconds;
        result = 31 * result + (success ? 1 : 0);
        return result;
    }
}

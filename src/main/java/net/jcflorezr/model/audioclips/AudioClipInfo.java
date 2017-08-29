package net.jcflorezr.model.audioclips;

public class AudioClipInfo {

    private String audioClipName;
    private int groupNumber;
    private int startPosition;
    private float startPositionInSeconds;
    private int endPosition;
    private float endPositionInSeconds;
    private float durationInSeconds;
    private int hours;
    private int minutes;
    private int seconds;
    private int milliseconds;

    public AudioClipInfo() {
    }

    public AudioClipInfo(int groupNumber, int startPosition, float startPositionInSeconds, int endPosition, float endPositionInSeconds, float durationInSeconds, String audioClipName, int hours, int minutes, int seconds, int milliseconds) {
        this.audioClipName = audioClipName;
        this.groupNumber = groupNumber;
        this.startPosition = startPosition;
        this.startPositionInSeconds = startPositionInSeconds;
        this.endPosition = endPosition;
        this.endPositionInSeconds = endPositionInSeconds;
        this.durationInSeconds = durationInSeconds;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
    }

    public String getAudioClipName() {
        return audioClipName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioClipInfo that = (AudioClipInfo) o;

        if (groupNumber != that.groupNumber) return false;
        if (startPosition != that.startPosition) return false;
        if (Float.compare(that.startPositionInSeconds, startPositionInSeconds) != 0) return false;
        if (endPosition != that.endPosition) return false;
        if (Float.compare(that.endPositionInSeconds, endPositionInSeconds) != 0) return false;
        if (Float.compare(that.durationInSeconds, durationInSeconds) != 0) return false;
        return audioClipName != null ? audioClipName.equals(that.audioClipName) : that.audioClipName == null;
    }

    @Override
    public int hashCode() {
        int result = audioClipName != null ? audioClipName.hashCode() : 0;
        result = 31 * result + groupNumber;
        result = 31 * result + startPosition;
        result = 31 * result + (startPositionInSeconds != +0.0f ? Float.floatToIntBits(startPositionInSeconds) : 0);
        result = 31 * result + endPosition;
        result = 31 * result + (endPositionInSeconds != +0.0f ? Float.floatToIntBits(endPositionInSeconds) : 0);
        result = 31 * result + (durationInSeconds != +0.0f ? Float.floatToIntBits(durationInSeconds) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AudioClipInfo{" +
                "audioClipName='" + audioClipName + '\'' +
                ", groupNumber=" + groupNumber +
                ", startPosition=" + startPosition +
                ", startPositionInSeconds=" + startPositionInSeconds +
                ", endPosition=" + endPosition +
                ", endPositionInSeconds=" + endPositionInSeconds +
                ", durationInSeconds=" + durationInSeconds +
                ", hours=" + hours +
                ", minutes=" + minutes +
                ", seconds=" + seconds +
                ", milliseconds=" + milliseconds +
                '}';
    }

    public static class SingleAudioSoundZoneInfoBuilder {

        private String suggestedAudioClipName;
        private int groupNumber;
        private int startPosition;
        private float startPositionInSeconds;
        private int endPosition;
        private float endPositionInSeconds;
        private float durationInSeconds;
        private int hours;
        private int minutes;
        private int seconds;
        private int milliseconds;

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

        public AudioClipInfo build() {
            return new AudioClipInfo(
                    groupNumber,
                    startPosition,
                    startPositionInSeconds,
                    endPosition,
                    endPositionInSeconds,
                    durationInSeconds,
                    suggestedAudioClipName,
                    hours,
                    minutes,
                    seconds,
                    milliseconds);
        }
    }
}

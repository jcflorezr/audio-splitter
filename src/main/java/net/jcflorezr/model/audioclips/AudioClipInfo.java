package net.jcflorezr.model.audioclips;

public class AudioClipInfo {

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

    public AudioClipInfo() {
    }

    public AudioClipInfo(int groupNumber, int startPosition, float startPositionInSeconds, int endPosition, float endPositionInSeconds, float durationInSeconds, String suggestedAudioClipName, int hours, int minutes, int seconds, int milliseconds) {
        this.suggestedAudioClipName = suggestedAudioClipName;
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

    public String getSuggestedAudioClipName() {
        return suggestedAudioClipName;
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

    
    public String toString() {
        return "AudioClipInfo{" +
                "suggestedAudioClipName='" + suggestedAudioClipName + '\'' +
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

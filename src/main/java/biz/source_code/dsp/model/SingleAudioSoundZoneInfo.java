package biz.source_code.dsp.model;

import biz.source_code.dsp.api.model.AudioSoundZoneInfo;

public class SingleAudioSoundZoneInfo implements AudioSoundZoneInfo {

    private String suggestedAudioFileName;
    private final int groupNumber;
    private int startPosition;
    private float startPositionInSeconds;
    private int endPosition;
    private float endPositionInSeconds;
    private float durationInSeconds;
    private int hours;
    private int minutes;
    private int seconds;
    private int milliseconds;
    private AudioFileWritingResult audioClipWritingResult;


    public SingleAudioSoundZoneInfo(int groupNumber, int startPosition, float startPositionInSeconds, int endPosition, float endPositionInSeconds, float durationInSeconds, String suggestedAudioFileName, int hours, int minutes, int seconds, int milliseconds) {
        this.suggestedAudioFileName = suggestedAudioFileName;
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

    @Override
    public String getSuggestedAudioFileName() {
        return suggestedAudioFileName;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public int getStartPosition() {
        return startPosition;
    }

    @Override
    public float getStartPositionInSeconds() {
        return startPositionInSeconds;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public float getEndPositionInSeconds() {
        return endPositionInSeconds;
    }

    @Override
    public float getDurationInSeconds() {
        return durationInSeconds;
    }

    @Override
    public int getHours() {
        return hours;
    }

    @Override
    public int getMinutes() {
        return minutes;
    }

    @Override
    public int getSeconds() {
        return seconds;
    }

    @Override
    public int getMilliseconds() {
        return milliseconds;
    }

    @Override
    public AudioFileWritingResult getAudioClipWritingResult() {
        return audioClipWritingResult;
    }

    @Override
    public void setAudioClipWritingResult(AudioFileWritingResult audioClipWritingResult) {
        this.audioClipWritingResult = audioClipWritingResult;
    }

    @Override
    public String toString() {
        return "SingleAudioSoundZoneInfo{" +
                "suggestedAudioFileName='" + suggestedAudioFileName + '\'' +
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
                ", audioClipWritingResult=" + audioClipWritingResult +
                '}';
    }

    public static class SingleAudioSoundZoneInfoBuilder {

        private String suggestedAudioFileName;
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

        public SingleAudioSoundZoneInfoBuilder suggestedAudioFileName(String suggestedAudioFileName) {
            this.suggestedAudioFileName = suggestedAudioFileName;
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

        public SingleAudioSoundZoneInfo build() {
            return new SingleAudioSoundZoneInfo(
                    groupNumber,
                    startPosition,
                    startPositionInSeconds,
                    endPosition,
                    endPositionInSeconds,
                    durationInSeconds,
                    suggestedAudioFileName,
                    hours,
                    minutes,
                    seconds,
                    milliseconds);
        }
    }
}

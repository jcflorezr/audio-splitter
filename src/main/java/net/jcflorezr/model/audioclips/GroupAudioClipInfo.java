package net.jcflorezr.model.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;

import java.util.Collections;
import java.util.List;

public class GroupAudioClipInfo implements AudioClipInfo {

    private String suggestedAudioFileName;
    private float startPositionInSeconds;
    private float durationInSeconds;
    private int hours;
    private int minutes;
    private int seconds;
    private int milliseconds;
    private int numSoundZones;
    private List<SingleAudioClipInfo> singleAudioClipsInfo;
    private AudioFileWritingResult audioClipWritingResult;

    public GroupAudioClipInfo(String suggestedAudioFileName, float startPositionInSeconds, float durationInSeconds, int hours, int minutes, int seconds, int milliseconds, int numSoundZones, List<SingleAudioClipInfo> singleAudioClipsInfo) {
        this.suggestedAudioFileName = suggestedAudioFileName;
        this.startPositionInSeconds = startPositionInSeconds;
        this.durationInSeconds = durationInSeconds;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
        this.numSoundZones = numSoundZones;
        this.singleAudioClipsInfo = singleAudioClipsInfo;
    }

    @Override
    public String getSuggestedAudioFileName() {
        return suggestedAudioFileName;
    }

    @Override
    public float getStartPositionInSeconds() {
        return startPositionInSeconds;
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

    public int getNumSoundZones() {
        return numSoundZones;
    }

    @Override
    public AudioFileWritingResult getAudioClipWritingResult() {
        return audioClipWritingResult;
    }

    @Override
    public void setAudioClipWritingResult(AudioFileWritingResult audioClipWritingResult) {
        this.audioClipWritingResult = audioClipWritingResult;
    }

    public List<SingleAudioClipInfo> getSingleAudioClipsInfo() {
        return Collections.unmodifiableList(singleAudioClipsInfo);
    }

    @Override
    public String toString() {
        return "GroupAudioClipInfo{" +
                "suggestedAudioFileName='" + suggestedAudioFileName + '\'' +
                ", startPositionInSeconds=" + startPositionInSeconds +
                ", durationInSeconds=" + durationInSeconds +
                ", hours=" + hours +
                ", minutes=" + minutes +
                ", seconds=" + seconds +
                ", milliseconds=" + milliseconds +
                ", numSoundZones=" + numSoundZones +
                ", audioClipWritingResult=" + audioClipWritingResult +
                '}';
    }

    public static class GroupAudioSoundZonesInfoBuilder {

        private String suggestedAudioFileName;
        private float startPositionInSeconds;
        private float durationInSeconds;
        private int hours;
        private int minutes;
        private int seconds;
        private int milliseconds;
        private int numSoundZones;
        private List<SingleAudioClipInfo> singleAudioSoundZonesInfo;

        public GroupAudioSoundZonesInfoBuilder suggestedAudioFileName(String suggestedAudioFileName) {
            this.suggestedAudioFileName = suggestedAudioFileName;
            return this;
        }

        public GroupAudioSoundZonesInfoBuilder startPositionInSeconds(float startPositionInSeconds) {
            this.startPositionInSeconds = startPositionInSeconds;
            return this;
        }

        public GroupAudioSoundZonesInfoBuilder durationInSeconds(float durationInSeconds) {
            this.durationInSeconds = durationInSeconds;
            return this;
        }

        public GroupAudioSoundZonesInfoBuilder hours(int hours) {
            this.hours = hours;
            return this;
        }

        public GroupAudioSoundZonesInfoBuilder minutes(int minutes) {
            this.minutes = minutes;
            return this;
        }

        public GroupAudioSoundZonesInfoBuilder seconds(int seconds) {
            this.seconds = seconds;
            return this;
        }

        public GroupAudioSoundZonesInfoBuilder milliseconds(int milliseconds) {
            this.milliseconds = milliseconds;
            return this;
        }

        public GroupAudioSoundZonesInfoBuilder numSoundZones(int numSoundZones) {
            this.numSoundZones = numSoundZones;
            return this;
        }

        public GroupAudioSoundZonesInfoBuilder singleAudioSoundZonesInfo(List<SingleAudioClipInfo> singleAudioSoundZonesInfo) {
            this.singleAudioSoundZonesInfo = singleAudioSoundZonesInfo;
            return this;
        }

        public GroupAudioClipInfo build() {
            return new GroupAudioClipInfo(
                    suggestedAudioFileName,
                    startPositionInSeconds,
                    durationInSeconds,
                    hours,
                    minutes,
                    seconds,
                    milliseconds,
                    numSoundZones,
                    singleAudioSoundZonesInfo);
        }
    }
}

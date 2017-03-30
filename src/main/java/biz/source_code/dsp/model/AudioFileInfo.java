package biz.source_code.dsp.model;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AudioFileInfo {

    private String originalAudioFileName;
    private String convertedAudioFileName;
    private String outputFileDirectoryPathWithFileName;
    private float audioDurationInSeconds;
    private List<SingleAudioSoundZoneInfo> singleAudioFileSoundZones;
    private List<GroupAudioSoundZonesInfo> groupedAudioFileSoundZones;
    private AudioSignal audioSignal;
    private AudioSignal separatorAudioSignal;

    public String getOriginalAudioFileName() {
        return originalAudioFileName;
    }

    public void setOriginalAudioFileName(String originalAudioFileName) {
        this.originalAudioFileName = originalAudioFileName;
    }

    public String getConvertedAudioFileName() {
        return convertedAudioFileName;
    }

    public void setConvertedAudioFileName(String convertedAudioFileName) {
        this.convertedAudioFileName = convertedAudioFileName;
    }

    public String getOutputFileDirectoryPathWithFileName() {
        return outputFileDirectoryPathWithFileName;
    }

    public void setOutputFilesDirectoryPathWithFileName(String outputFileDirectoryPathWithFileName) {
        this.outputFileDirectoryPathWithFileName = outputFileDirectoryPathWithFileName;
    }

    public float getAudioDurationInSeconds() {
        return audioDurationInSeconds;
    }

    public void setAudioDurationInSeconds(float audioDurationInSeconds) {
        this.audioDurationInSeconds = audioDurationInSeconds;
    }

    public List<SingleAudioSoundZoneInfo> getSingleAudioFileSoundZones() {
        return singleAudioFileSoundZones;
    }

    public void setSingleAudioFileSoundZones(List<SingleAudioSoundZoneInfo> singleAudioFileSoundZones) {
        this.singleAudioFileSoundZones = singleAudioFileSoundZones;
    }

    public List<GroupAudioSoundZonesInfo> getGroupedAudioFileSoundZones() {
        return groupedAudioFileSoundZones;
    }

    public void setGroupedAudioFileSoundZones(List<GroupAudioSoundZonesInfo> groupedAudioFileSoundZones) {
        this.groupedAudioFileSoundZones = groupedAudioFileSoundZones;
    }

    public AudioSignal getAudioSignal() {
        return audioSignal;
    }

    public void setAudioSignal(AudioSignal audioSignal) {
        this.audioSignal = audioSignal;
    }

    public boolean audioFileWasConverted() {
        return !originalAudioFileName.equals(convertedAudioFileName) &&
                Files.exists(Paths.get(convertedAudioFileName));
    }

    public void setSeparatorAudioSignal(AudioSignal separatorAudioSignal) {
        this.separatorAudioSignal = separatorAudioSignal;
    }

    public AudioSignal getSeparatorAudioSignal() {
        return separatorAudioSignal;
    }
}

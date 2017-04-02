package biz.source_code.dsp.model;

import biz.source_code.dsp.util.AudioFormatsSupported;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AudioFileInfo {

    private AudioFileLocation audioFileLocation;
//    private String originalAudioFileName;
//    private String outputFileDirectoryPathWithFileName;
    private String convertedAudioFileName;
//    private float audioDurationInSeconds;
    private List<SingleAudioSoundZoneInfo> singleAudioSoundZones;
    private List<GroupAudioSoundZonesInfo> groupedAudioFileSoundZones;
    private AudioContent audioContent;
//    private AudioSignal audioSignal;
//    private AudioSignal separatorAudioSignal;
//    private AudioFileWritingResult audioFileWritingResult;

    public AudioFileInfo(AudioFileLocation audioFileLocation) {
        this.audioFileLocation = audioFileLocation;
    }

    public AudioFileLocation getAudioFileLocation() {
        return audioFileLocation;
    }

    //    public String getOriginalAudioFileName() {
//        return originalAudioFileName;
//    }
//
//    public void setOriginalAudioFileName(String originalAudioFileName) {
//        this.originalAudioFileName = originalAudioFileName;
//    }

    public String getConvertedAudioFileName() {
        return convertedAudioFileName;
    }

    public void setConvertedAudioFileName(String convertedAudioFileName) {
        this.convertedAudioFileName = convertedAudioFileName;
    }

//    public String getOutputFileDirectoryPathWithFileName() {
//        return outputFileDirectoryPathWithFileName;
//    }
//
//    public void setOutputFilesDirectoryPathWithFileName(String outputFileDirectoryPathWithFileName) {
//        this.outputFileDirectoryPathWithFileName = outputFileDirectoryPathWithFileName;
//    }

//    public float getAudioDurationInSeconds() {
//        return audioDurationInSeconds;
//    }
//
//    public void setAudioDurationInSeconds(float audioDurationInSeconds) {
//        this.audioDurationInSeconds = audioDurationInSeconds;
//    }

    public List<SingleAudioSoundZoneInfo> getSingleAudioSoundZones() {
        return singleAudioSoundZones;
    }

    public void setSingleAudioSoundZones(List<SingleAudioSoundZoneInfo> singleAudioSoundZones) {
        this.singleAudioSoundZones = singleAudioSoundZones;
    }

    public List<GroupAudioSoundZonesInfo> getGroupedAudioFileSoundZones() {
        return groupedAudioFileSoundZones;
    }

    public void setGroupedAudioFileSoundZones(List<GroupAudioSoundZonesInfo> groupedAudioFileSoundZones) {
        this.groupedAudioFileSoundZones = groupedAudioFileSoundZones;
    }

    public void setAudioFileLocation(AudioFileLocation audioFileLocation) {
        this.audioFileLocation = audioFileLocation;
    }

    public AudioContent getAudioContent() {
        return audioContent;
    }

    public void setAudioContent(AudioContent audioContent) {
        this.audioContent = audioContent;
    }

//    public void setAudioFileWritingResult(AudioFileWritingResult audioFileWritingResult) {
//        this.audioFileWritingResult = audioFileWritingResult;
//    }
//
//    public AudioFileWritingResult getAudioFileWritingResult() {
//        return audioFileWritingResult;
//    }

    public boolean audioFileWasConverted() {
        return !audioFileLocation.getAudioFileName().equals(convertedAudioFileName) &&
                Files.exists(Paths.get(convertedAudioFileName));
    }

    public OutputAudioClipsConfig getOutputAudioClipsConfig(AudioFormatsSupported audioFormat, boolean asMono) {
        return new OutputAudioClipsConfig(
                audioFileLocation.getOutputAudioClipsDirectoryPath(),
                audioContent,
                audioFormat.getExtension(),
                asMono);
    }
}

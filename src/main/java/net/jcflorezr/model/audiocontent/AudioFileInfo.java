package net.jcflorezr.model.audiocontent;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.model.request.AudioFileLocation;
import net.jcflorezr.model.audioclips.GroupAudioClipInfo;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audioclips.SingleAudioClipInfo;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class AudioFileInfo {

    private AudioFileLocation audioFileLocation;
    private String convertedAudioFileName;
    private List<SingleAudioClipInfo> singleAudioSoundZones;
    private List<GroupAudioClipInfo> groupedAudioFileSoundZones;
    private AudioContent audioContent;

    public AudioFileInfo(AudioFileLocation audioFileLocation) {
        this.audioFileLocation = audioFileLocation;
    }

    public AudioFileLocation getAudioFileLocation() {
        return audioFileLocation;
    }

    public String getConvertedAudioFileName() {
        return convertedAudioFileName;
    }

    public void setConvertedAudioFileName(String convertedAudioFileName) {
        this.convertedAudioFileName = convertedAudioFileName;
    }

    public List<SingleAudioClipInfo> getSingleAudioSoundZones() {
        return singleAudioSoundZones;
    }

    public void setSingleAudioSoundZones(List<SingleAudioClipInfo> singleAudioSoundZones) {
        this.singleAudioSoundZones = singleAudioSoundZones;
    }

    public List<GroupAudioClipInfo> getGroupedAudioFileSoundZones() {
        return groupedAudioFileSoundZones;
    }

    public void setGroupedAudioFileSoundZones(List<GroupAudioClipInfo> groupedAudioFileSoundZones) {
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

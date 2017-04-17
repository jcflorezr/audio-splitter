package net.jcflorezr.model.audiocontent;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audioclips.SingleAudioClipInfo;
import net.jcflorezr.model.request.AudioFileLocation;

import java.util.List;

public class AudioFileInfo {

    private AudioFileLocation audioFileLocation;
    private String convertedAudioFileName;
    private List<SingleAudioClipInfo> singleAudioClipsInfo;
    private List<AudioClipsWritingResult> audioClipsWritingResult;
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

    public List<SingleAudioClipInfo> getSingleAudioClipsInfo() {
        return singleAudioClipsInfo;
    }

    public void setSingleAudioClipsInfo(List<SingleAudioClipInfo> singleAudioClipsInfo) {
        this.singleAudioClipsInfo = singleAudioClipsInfo;
    }

    public List<AudioClipsWritingResult> getAudioClipsWritingResult() {
        return audioClipsWritingResult;
    }

    public void setAudioClipsWritingResult(List<AudioClipsWritingResult> audioClipsWritingResult) {
        this.audioClipsWritingResult = audioClipsWritingResult;
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
        return !audioFileLocation.getAudioFileName().equals(convertedAudioFileName);
    }

    public OutputAudioClipsConfig getOutputAudioClipsConfig(AudioFormatsSupported audioFormat, boolean asMono) {
        return new OutputAudioClipsConfig(
                audioFileLocation.getOutputAudioClipsDirectoryPath(),
                audioContent,
                audioFormat.getExtension(),
                asMono);
    }
}

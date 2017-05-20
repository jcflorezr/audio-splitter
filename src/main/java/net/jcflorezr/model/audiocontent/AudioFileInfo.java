package net.jcflorezr.model.audiocontent;

import biz.source_code.dsp.util.AudioFormatsSupported;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.request.AudioFileLocation;

import java.util.List;

public class AudioFileInfo {

    private AudioFileLocation audioFileLocation;
    private List<AudioClipInfo> audioClipsInfo;
    private AudioContent audioContent;

    public AudioFileInfo(AudioFileLocation audioFileLocation) {
        this.audioFileLocation = audioFileLocation;
    }

    public AudioFileLocation getAudioFileLocation() {
        return audioFileLocation;
    }

    public List<AudioClipInfo> getAudioClipsInfo() {
        return audioClipsInfo;
    }

    public void setAudioClipsInfo(List<AudioClipInfo> audioClipsInfo) {
        this.audioClipsInfo = audioClipsInfo;
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

    @JsonIgnore
    public OutputAudioClipsConfig getOutputAudioClipsConfig(AudioFormatsSupported audioFormat, boolean asMono, boolean withSeparator) {
        return new OutputAudioClipsConfig(
                audioFileLocation.getOutputAudioClipsDirectoryPath(),
                audioContent,
                audioFormat.getExtension(),
                asMono,
                withSeparator);
    }
}

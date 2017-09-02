package net.jcflorezr.model.audiocontent;

import biz.source_code.dsp.util.AudioFormatsSupported;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.jcflorezr.model.audioclips.AudioFileClip;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.request.AudioFileBasicInfo;

import java.util.List;

public class AudioFileCompleteInfo {

    private AudioFileBasicInfo audioFileBasicInfo;
    private List<AudioFileClip> audioClipsInfo;
    private AudioContent audioContent;

    public AudioFileCompleteInfo(AudioFileBasicInfo audioFileBasicInfo) {
        this.audioFileBasicInfo = audioFileBasicInfo;
    }

    public AudioFileCompleteInfo(AudioFileBasicInfo audioFileBasicInfo, List<AudioFileClip> audioClipsInfo, AudioContent audioContent) {
        this.audioFileBasicInfo = audioFileBasicInfo;
        this.audioClipsInfo = audioClipsInfo;
        this.audioContent = audioContent;
    }

    public AudioFileBasicInfo getAudioFileBasicInfo() {
        return audioFileBasicInfo;
    }

    public List<AudioFileClip> getAudioClipsInfo() {
        return audioClipsInfo;
    }

    public void setAudioClipsInfo(List<AudioFileClip> audioClipsInfo) {
        this.audioClipsInfo = audioClipsInfo;
    }

    public void setAudioFileBasicInfo(AudioFileBasicInfo audioFileBasicInfo) {
        this.audioFileBasicInfo = audioFileBasicInfo;
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
                audioFileBasicInfo.getOutputAudioClipsDirectoryPath(),
                audioContent,
                audioFormat.getExtension(),
                asMono,
                withSeparator);
    }
}

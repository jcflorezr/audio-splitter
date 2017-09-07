package net.jcflorezr.model.audiocontent;

import biz.source_code.dsp.util.AudioFormatsSupported;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.jcflorezr.model.audioclips.AudioFileClipEntity;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.endpoint.AudioFileBasicInfoEntity;

import java.util.List;

public class AudioFileCompleteInfo {

    private AudioFileBasicInfoEntity audioFileBasicInfoEntity;
    private List<AudioFileClipEntity> audioClipsInfo;
    private AudioContent audioContent;

    public AudioFileCompleteInfo(AudioFileBasicInfoEntity audioFileBasicInfoEntity) {
        this.audioFileBasicInfoEntity = audioFileBasicInfoEntity;
    }

    public AudioFileCompleteInfo(AudioFileBasicInfoEntity audioFileBasicInfoEntity, List<AudioFileClipEntity> audioClipsInfo, AudioContent audioContent) {
        this.audioFileBasicInfoEntity = audioFileBasicInfoEntity;
        this.audioClipsInfo = audioClipsInfo;
        this.audioContent = audioContent;
    }

    public AudioFileBasicInfoEntity getAudioFileBasicInfoEntity() {
        return audioFileBasicInfoEntity;
    }

    public List<AudioFileClipEntity> getAudioClipsInfo() {
        return audioClipsInfo;
    }

    public void setAudioClipsInfo(List<AudioFileClipEntity> audioClipsInfo) {
        this.audioClipsInfo = audioClipsInfo;
    }

    public void setAudioFileBasicInfoEntity(AudioFileBasicInfoEntity audioFileBasicInfoEntity) {
        this.audioFileBasicInfoEntity = audioFileBasicInfoEntity;
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
                audioFileBasicInfoEntity.getOutputAudioClipsDirectoryPath(),
                audioContent,
                audioFormat.getExtension(),
                asMono,
                withSeparator);
    }
}

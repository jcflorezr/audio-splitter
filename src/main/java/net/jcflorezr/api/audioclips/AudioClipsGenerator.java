package net.jcflorezr.api.audioclips;

import net.jcflorezr.model.audioclips.AudioFileClipResultEntity;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;

import java.util.List;

public interface AudioClipsGenerator {

    List<AudioFileClipResultEntity> generateAudioClip(String audioFileName, AudioFileCompleteInfo audioFileCompleteInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup);

}

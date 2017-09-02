package net.jcflorezr.api.audioclips;

import net.jcflorezr.model.audioclips.AudioFileClipResult;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;

import java.util.List;

public interface AudioClipsGenerator {

    List<AudioFileClipResult> generateAudioClip(String audioFileName, AudioFileCompleteInfo audioFileCompleteInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup);

}

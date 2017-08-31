package net.jcflorezr.api.audioclips;

import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioFileInfo;

import java.util.List;

public interface AudioClipsGenerator {

    List<AudioClipsWritingResult> generateAudioClip(String audioFileName, AudioFileInfo audioFileInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup);

}

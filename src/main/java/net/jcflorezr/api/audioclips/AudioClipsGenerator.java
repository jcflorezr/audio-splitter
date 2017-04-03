package net.jcflorezr.api.audioclips;

import net.jcflorezr.model.audioclips.AudioClipInfo;
import biz.source_code.dsp.model.AudioFileWritingResult;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;

public interface AudioClipsGenerator {

    AudioFileWritingResult generateAudioClip(AudioClipInfo audioClipInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup);

}

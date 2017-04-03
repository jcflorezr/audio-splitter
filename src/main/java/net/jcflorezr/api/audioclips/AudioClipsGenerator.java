package net.jcflorezr.api.audioclips;

import net.jcflorezr.api.model.AudioSoundZoneInfo;
import biz.source_code.dsp.model.AudioFileWritingResult;
import net.jcflorezr.model.OutputAudioClipsConfig;

public interface AudioClipsGenerator {

    AudioFileWritingResult generateSoundZoneAudioFile(AudioSoundZoneInfo audioSoundZoneInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup);

}

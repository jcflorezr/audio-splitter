package biz.source_code.dsp.api.audioclips;

import biz.source_code.dsp.api.model.AudioSoundZoneInfo;
import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.model.OutputAudioClipsConfig;

public interface AudioClipsGenerator {

    AudioFileWritingResult generateSoundZoneAudioFile(AudioSoundZoneInfo audioSoundZoneInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup);

}

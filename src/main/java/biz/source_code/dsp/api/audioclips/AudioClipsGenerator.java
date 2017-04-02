package biz.source_code.dsp.api.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.model.GroupAudioSoundZonesInfo;
import biz.source_code.dsp.model.OutputAudioClipsConfig;
import biz.source_code.dsp.model.SingleAudioSoundZoneInfo;

public interface AudioClipsGenerator {

    AudioFileWritingResult generateSingleSoundZoneAudioFile(SingleAudioSoundZoneInfo singleAudioSoundZoneInfo, OutputAudioClipsConfig outputAudioClipsConfig);

    AudioFileWritingResult generateGroupSoundZonesAudioFile(GroupAudioSoundZonesInfo groupAudioSoundZonesInfo, OutputAudioClipsConfig outputAudioClipsConfig);

}

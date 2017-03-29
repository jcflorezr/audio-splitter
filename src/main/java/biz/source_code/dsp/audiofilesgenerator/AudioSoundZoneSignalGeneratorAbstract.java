package biz.source_code.dsp.audiofilesgenerator;

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.model.GroupAudioSoundZonesInfo;
import biz.source_code.dsp.model.SingleAudioSoundZoneInfo;
import biz.source_code.dsp.model.AudioFileWritingResult;

abstract class AudioSoundZoneSignalGeneratorAbstract {

    abstract AudioFileWritingResult generateSingleSoundZoneAudioFile(SingleAudioSoundZoneInfo singleAudioSoundZoneInfo, String outputFileDirectoryPath, AudioSignal originalAudioFileSignal, String extension, boolean mono);

    abstract AudioFileWritingResult generateGroupSoundZonesAudioFile(GroupAudioSoundZonesInfo groupAudioSoundZonesInfo, String outputFileDirectoryPath, AudioSignal originalAudioFileSignal, AudioSignal groupSeparatorAudioFileSignal, String extension, boolean mono);

}

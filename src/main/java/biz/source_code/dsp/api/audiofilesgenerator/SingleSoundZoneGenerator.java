package biz.source_code.dsp.api.audiofilesgenerator;

import biz.source_code.dsp.api.model.AudioSoundZoneInfo;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.model.AudioFileWritingResult;

public interface SingleSoundZoneGenerator {

    AudioFileWritingResult generateAudioFile(AudioSoundZoneInfo audioSoundZoneInfo, String outputFileDirectoryPath, AudioSignal audioFileSignal);

    AudioFileWritingResult generateAudioMonoFile(AudioSoundZoneInfo audioSoundZoneInfo, String outputFileDirectoryPath, AudioSignal audioFileSignal);

}

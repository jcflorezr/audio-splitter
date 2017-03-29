package biz.source_code.dsp.audiofilesgenerator;

import biz.source_code.dsp.api.audiofilesgenerator.GroupSoundZonesGenerator;
import biz.source_code.dsp.api.model.AudioSoundZoneInfo;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.model.GroupAudioSoundZonesInfo;
import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.util.AudioFormatsSupported;

public class GroupSoundZonesWavGenerator extends AudioSoundZoneSignalGenerator implements GroupSoundZonesGenerator {

    @Override
    public AudioFileWritingResult generateAudioFile(AudioSoundZoneInfo audioSoundZoneInfo, String outputFileDirectoryPath, AudioSignal audioFileSignal, AudioSignal separatorAudioFileSignal) {
        GroupAudioSoundZonesInfo groupAudioSoundZonesInfo = (GroupAudioSoundZonesInfo) audioSoundZoneInfo;
        return generateGroupSoundZonesAudioFile(
                groupAudioSoundZonesInfo,
                outputFileDirectoryPath,
                audioFileSignal,
                separatorAudioFileSignal,
                AudioFormatsSupported.WAV.getExtension(),
                false);
    }

    @Override
    public AudioFileWritingResult generateAudioMonoFile(AudioSoundZoneInfo audioSoundZoneInfo, String outputFileDirectoryPath, AudioSignal audioFileSignal, AudioSignal separatorAudioFileSignal) {
        GroupAudioSoundZonesInfo groupAudioSoundZonesInfo = (GroupAudioSoundZonesInfo) audioSoundZoneInfo;
        return generateGroupSoundZonesAudioFile(
                groupAudioSoundZonesInfo,
                outputFileDirectoryPath,
                audioFileSignal,
                separatorAudioFileSignal,
                AudioFormatsSupported.WAV.getExtension(),
                true);
    }

}

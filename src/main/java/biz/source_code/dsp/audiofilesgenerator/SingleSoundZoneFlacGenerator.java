package biz.source_code.dsp.audiofilesgenerator;

import biz.source_code.dsp.api.audiofilesgenerator.SingleSoundZoneGenerator;
import biz.source_code.dsp.api.model.AudioSoundZoneInfo;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.model.SingleAudioSoundZoneInfo;
import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.util.AudioFormatsSupported;


public class SingleSoundZoneFlacGenerator extends AudioSoundZoneSignalGenerator implements SingleSoundZoneGenerator {

    @Override
    public AudioFileWritingResult generateAudioFile(AudioSoundZoneInfo audioSoundZoneInfo, String outputFileDirectoryPath, AudioSignal audioFileSignal) {
        SingleAudioSoundZoneInfo singleAudioFileSoundZone = (SingleAudioSoundZoneInfo) audioSoundZoneInfo;
        return generateSingleSoundZoneAudioFile(
                singleAudioFileSoundZone,
                outputFileDirectoryPath,
                audioFileSignal,
                AudioFormatsSupported.FLAC.getExtension(),
                false);
    }

    @Override
    public AudioFileWritingResult generateAudioMonoFile(AudioSoundZoneInfo audioSoundZoneInfo, String outputFileDirectoryPath, AudioSignal audioFileSignal) {
        SingleAudioSoundZoneInfo singleAudioFileSoundZone = (SingleAudioSoundZoneInfo) audioSoundZoneInfo;
        return generateSingleSoundZoneAudioFile(
                singleAudioFileSoundZone,
                outputFileDirectoryPath,
                audioFileSignal,
                AudioFormatsSupported.FLAC.getExtension(),
                true);
    }

}

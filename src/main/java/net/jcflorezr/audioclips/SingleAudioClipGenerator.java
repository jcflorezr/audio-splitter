package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audioclips.SingleAudioClipInfo;

class SingleAudioClipGenerator {

    private AudioIo audioIo = new AudioIo();

    AudioFileWritingResult generateAudioClip(SingleAudioClipInfo singleAudioSoundZoneInfo, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioSignal originalAudioFileSignal = outputAudioClipsConfig.getAudioContent().getOriginalAudioSignal();
        boolean asMono = outputAudioClipsConfig.isMono();
        AudioSignal soundZoneAudioSignal = asMono ? getSingleSoundZoneAudioSignalAsMono(originalAudioFileSignal)
                                                  : originalAudioFileSignal;
        String outputFileName = outputAudioClipsConfig.getOutputAudioClipsDirectoryPath() + singleAudioSoundZoneInfo.getSuggestedAudioClipName();
        int startPosition = singleAudioSoundZoneInfo.getStartPosition();
        int soundZoneLength = singleAudioSoundZoneInfo.getEndPosition() - singleAudioSoundZoneInfo.getStartPosition();
        return audioIo.saveAudioFile(outputFileName, outputAudioClipsConfig.getAudioFormatExtension(), soundZoneAudioSignal, startPosition, soundZoneLength);
    }

    private AudioSignal getSingleSoundZoneAudioSignalAsMono(AudioSignal originalAudioFileSignal) {
        return new AudioSignal(originalAudioFileSignal.getSamplingRate(), new float[][]{originalAudioFileSignal.getData()[0]});
    }

}

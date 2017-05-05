package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioSignal;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;

class SingleAudioClipSignalGenerator {

    AudioSignal generateAudioClip(AudioClipInfo singleAudioSoundZoneInfo, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioSignal originalAudioFileSignal = outputAudioClipsConfig.getAudioContent().getOriginalAudioSignal();
        boolean asMono = outputAudioClipsConfig.isMono();
        return asMono ? getSingleSoundZoneAudioSignalAsMono(originalAudioFileSignal) : originalAudioFileSignal;
    }

    private AudioSignal getSingleSoundZoneAudioSignalAsMono(AudioSignal originalAudioFileSignal) {
        return new AudioSignal(originalAudioFileSignal.getSamplingRate(), new float[][]{originalAudioFileSignal.getData()[0]});
    }

}

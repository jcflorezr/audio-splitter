package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioSignal;
import net.jcflorezr.model.audioclips.AudioFileClipEntity;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import org.springframework.stereotype.Service;

import static java.util.Arrays.copyOfRange;

@Service
class SingleAudioClipSignalGenerator {

    private static final int MONO_CHANNELS = 1;
    private static final int STEREO_CHANNELS = 2;

    AudioSignal generateAudioClip(AudioFileClipEntity audioFileClipEntity, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioSignal originalAudioFileSignal = outputAudioClipsConfig.getAudioContent().getOriginalAudioSignal();
        boolean asMono = outputAudioClipsConfig.isMono();
        float[][] audioClipSignalData = getAudioClipSignalData(audioFileClipEntity, originalAudioFileSignal, asMono);
        return new AudioSignal(originalAudioFileSignal.getSamplingRate(), audioClipSignalData);
    }

    private float[][] getAudioClipSignalData(AudioFileClipEntity audioFileClipEntity, AudioSignal originalAudioFileSignal, boolean mono) {
        int channels = mono ? MONO_CHANNELS : STEREO_CHANNELS;
        int originalAudioChannels = originalAudioFileSignal.getChannels();
        float[][] audioClipSignalData = new float[channels][];
        for (int i = 0; i < channels; i++) {
            float[] currentChannelData = originalAudioChannels < channels ? originalAudioFileSignal.getData()[0]
                    : originalAudioFileSignal.getData()[i];
            audioClipSignalData[i] = copyOfRange(currentChannelData, audioFileClipEntity.getStartPosition(), audioFileClipEntity.getEndPosition());
        }
        return audioClipSignalData;
    }

}

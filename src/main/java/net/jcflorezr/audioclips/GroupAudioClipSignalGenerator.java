package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioSignal;
import net.jcflorezr.model.audioclips.AudioFileClip;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioContent;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.toList;

@Service
class GroupAudioClipSignalGenerator {

    private static final int MONO_CHANNELS = 1;
    private static final int STEREO_CHANNELS = 2;

    AudioSignal generateAudioClip(List<AudioFileClip> groupAudioClipsInfo, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioContent audioContent = outputAudioClipsConfig.getAudioContent();
        float[][] finalAudioSignalData = generateAudioClipSignals(groupAudioClipsInfo,
                        audioContent.getOriginalAudioSignal(),
                        outputAudioClipsConfig.isMono(),
                        outputAudioClipsConfig.isWithSeparator());
        int audioSamplingRate = audioContent.getOriginalAudioSamplingRate();
        return new AudioSignal(audioSamplingRate, finalAudioSignalData);
    }

    private float[][] generateAudioClipSignals(List<AudioFileClip> groupAudioClipsInfo, AudioSignal originalAudioFileSignal, boolean mono, boolean withSeparator) {
        Stream<float[][]> audioClipsSignalsStream = groupAudioClipsInfo.stream()
                .map(audioClipInfo -> getAudioClipSignalData(audioClipInfo, originalAudioFileSignal, mono));
        if (withSeparator) {
            return generateAudioClipSignalsWithSeparator(groupAudioClipsInfo, originalAudioFileSignal, audioClipsSignalsStream.collect(toList()), mono);
        } else {
            return audioClipsSignalsStream.reduce((clip1, clip2) -> joinAudioSignals(clip1, clip2)).orElse(new float[][]{});
        }
    }

    private float[][] generateAudioClipSignalsWithSeparator(List<AudioFileClip> groupAudioClipsInfo, AudioSignal originalAudioFileSignal, List<float[][]> audioClipsSignals, boolean mono) {
        float [][] groupSeparatorSignal = retrieveSeparatorAudioSignalData(originalAudioFileSignal, mono);
        List<float[][]> groupSeparatorSignals = Stream.generate(() -> groupSeparatorSignal)
                .limit(groupAudioClipsInfo.size())
                .collect(toList());
        return mergeClipSignalToSeparatorSignal(audioClipsSignals, groupSeparatorSignals).stream()
                .reduce((clip1, clip2) -> joinAudioSignals(clip1, clip2))
                .orElse(new float[][]{});
    }

    private float[][] getAudioClipSignalData(AudioFileClip audioFileClip, AudioSignal originalAudioFileSignal, boolean mono) {
        int channels = mono ? MONO_CHANNELS : STEREO_CHANNELS;
        int originalAudioChannels = originalAudioFileSignal.getChannels();
        float[][] audioClipSignalData = new float[channels][];
        for (int i = 0; i < channels; i++) {
            float[] currentChannelData = originalAudioChannels < channels ? originalAudioFileSignal.getData()[0]
                                                                          : originalAudioFileSignal.getData()[i];
            audioClipSignalData[i] = copyOfRange(currentChannelData, audioFileClip.getStartPosition(), audioFileClip.getEndPosition());
        }
        return audioClipSignalData;
    }

    private List<float[][]> mergeClipSignalToSeparatorSignal(List<float[][]> audioClipSignal, List<float[][]> separatorSignal) {
        List<float[][]> finalAudioClipSignal = new ArrayList<>();
        int audioClipSignalSize = audioClipSignal.size();
        int separatorSignalSize = separatorSignal.size();
        for (int i = 0; i < audioClipSignalSize; i++) {
            finalAudioClipSignal.add(audioClipSignal.get(i));
            if (i < separatorSignalSize) {
                finalAudioClipSignal.add(separatorSignal.get(i));
            }
        }
        return finalAudioClipSignal;
    }

    private float[][] joinAudioSignals(float[][] audioClipSignal, float[][] groupSeparatorSignal) {
        float[][] jointSignalData = new float[audioClipSignal.length][];
        for (int i = 0; i < audioClipSignal.length; i++) {
            jointSignalData[i] = ArrayUtils.addAll(audioClipSignal[i], groupSeparatorSignal[i]);
        }
        return jointSignalData;
    }

    private float[][] retrieveSeparatorAudioSignalData(AudioSignal originalAudioSignal, boolean mono) {
        int channels = mono ? MONO_CHANNELS : STEREO_CHANNELS;
        int samplingRate = originalAudioSignal.getSamplingRate();
        int separatorSize = samplingRate / 2; // 500ms
        float[][] separatorSignalData = new float[channels][separatorSize];
        for (float[] current : separatorSignalData) {
            Arrays.fill(current, 0.0f);
        }
        return separatorSignalData;
    }

}

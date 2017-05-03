package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioContent;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.toList;

class GroupAudioClipGenerator {

    private static final int MONO_CHANNELS = 1;
    private static final int STEREO_CHANNELS = 2;

    private AudioIo audioIo = new AudioIo();

    AudioFileWritingResult generateAudioClip(List<AudioClipInfo> groupAudioClipsInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean withSeparator) {
        AudioContent audioContent = outputAudioClipsConfig.getAudioContent();
        float[][] finalAudioSignalData = generateAudioClipSignals(groupAudioClipsInfo,
                        audioContent.getOriginalAudioSignal(),
                        outputAudioClipsConfig.isMono(),
                        withSeparator);
        int audioSamplingRate = audioContent.getOriginalAudioSamplingRate();
        String suggestedAudioClipName = groupAudioClipsInfo.get(0).getSuggestedAudioClipName();
        String groupAudioFileNameAndPath = outputAudioClipsConfig.getOutputAudioClipsDirectoryPath() + suggestedAudioClipName;
        AudioSignal finalAudioSignal = new AudioSignal(audioSamplingRate, finalAudioSignalData);
        return audioIo.saveAudioFile(groupAudioFileNameAndPath, outputAudioClipsConfig.getAudioFormatExtension(), finalAudioSignal);
    }

    private float[][] generateAudioClipSignals(List<AudioClipInfo> groupAudioClipsInfo, AudioSignal originalAudioFileSignal, boolean mono, boolean withSeparator) {
        Stream<float[][]> audioClipsSignalsStream = groupAudioClipsInfo.stream()
                .map(audioClipInfo -> mono ? getAudioClipSignalDataAsMono(audioClipInfo, originalAudioFileSignal)
                                           : getAudioClipSignalDataAsStereo(audioClipInfo, originalAudioFileSignal));
        if (withSeparator) {
            return generateAudioClipSignalsWithSeparator(groupAudioClipsInfo, originalAudioFileSignal, audioClipsSignalsStream.collect(toList()));
        } else {
            return audioClipsSignalsStream.reduce((clip1, clip2) -> joinAudioSignals(clip1, clip2)).orElse(new float[][]{});
        }
    }

    private float[][] generateAudioClipSignalsWithSeparator(List<AudioClipInfo> groupAudioClipsInfo, AudioSignal originalAudioFileSignal, List<float[][]> audioClipsSignals) {
        float [][] groupSeparatorSignal = retrieveSeparatorAudioSignalData(originalAudioFileSignal);
        List<float[][]> groupSeparatorSignals = Stream.generate(() -> groupSeparatorSignal)
                .limit(groupAudioClipsInfo.size())
                .collect(toList());
        return mergeClipSignalToSeparatorSignal(audioClipsSignals, groupSeparatorSignals).stream()
                .reduce((clip1, clip2) -> joinAudioSignals(clip1, clip2))
                .orElse(new float[][]{});
    }

    private float[][] getAudioClipSignalDataAsStereo(AudioClipInfo audioClipInfo, AudioSignal originalAudioFileSignal) {
        return getAudioClipSignalData(audioClipInfo, originalAudioFileSignal, false);
    }

    private float[][] getAudioClipSignalDataAsMono(AudioClipInfo audioClipInfo, AudioSignal originalAudioFileSignal) {
        return getAudioClipSignalData(audioClipInfo, originalAudioFileSignal, true);
    }

    private float[][] getAudioClipSignalData(AudioClipInfo audioClipInfo, AudioSignal originalAudioFileSignal, boolean mono) {
        int channels = mono ? MONO_CHANNELS : STEREO_CHANNELS;
        int originalAudioChannels = originalAudioFileSignal.getChannels();
        float[][] audioClipSignalData = new float[channels][];
        for (int i = 0; i < channels; i++) {
            float[] currentChannelData = originalAudioChannels < channels ? originalAudioFileSignal.getData()[0]
                    : originalAudioFileSignal.getData()[i];
            audioClipSignalData[i] = copyOfRange(currentChannelData, audioClipInfo.getStartPosition(), audioClipInfo.getEndPosition());
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

    private float[][] retrieveSeparatorAudioSignalData(AudioSignal originalAudioSignal) {
        int channels = originalAudioSignal.getChannels();
        int samplingRate = originalAudioSignal.getSamplingRate();
        int separatorSize = samplingRate / 2; // 500ms
        float[][] separatorSignalData = new float[channels][separatorSize];
        for (float[] current : separatorSignalData) {
            Arrays.fill(current, 0.0f);
        }
        return separatorSignalData;
    }

}

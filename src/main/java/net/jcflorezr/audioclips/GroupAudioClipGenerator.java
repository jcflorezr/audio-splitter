package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audioclips.GroupAudioClipInfo;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audioclips.SingleAudioClipInfo;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.toList;

class GroupAudioClipGenerator {

    private static final int MONO_CHANNELS = 1;
    private static final int STEREO_CHANNELS = 2;

    private AudioIo audioIo = new AudioIo();

    AudioFileWritingResult generateAudioClip(GroupAudioClipInfo groupAudioClipInfo, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioContent audioContent = outputAudioClipsConfig.getAudioContent();
        List<float[][]> audioClipsSignalsWithSeparator =
                generateAudioClipSignalsWithSeparator(groupAudioClipInfo,
                        audioContent.getOriginalAudioSignal(),
                        audioContent.getSeparatorAudioSignal(),
                        outputAudioClipsConfig.isMono());
        float[][] finalAudioSignalData = audioClipsSignalsWithSeparator.stream()
                .reduce((clip1, clip2) -> joinSeparatorToAudioClip(clip1, clip2))
                .orElse(new float[][]{});
        int audioSamplingRate = audioContent.getOriginalAudioSamplingRate();
        String groupAudioFileNameAndPath = outputAudioClipsConfig.getOutputAudioClipsDirectoryPath()
                + groupAudioClipInfo.getSuggestedAudioFileName();
        AudioSignal finalAudioSignal = new AudioSignal(audioSamplingRate, finalAudioSignalData);
        return audioIo.saveAudioFile(groupAudioFileNameAndPath, outputAudioClipsConfig.getAudioFormatExtension(), finalAudioSignal);
    }

    private List<float[][]> generateAudioClipSignalsWithSeparator(GroupAudioClipInfo groupAudioClipInfo, AudioSignal originalAudioFileSignal, AudioSignal groupSeparatorAudioFileSignal, boolean mono) {
        List<SingleAudioClipInfo> singleAudioClipsInfo = groupAudioClipInfo.getSingleAudioClipsInfo();
        List<float[][]> audioClipsAudioSignals = singleAudioClipsInfo.stream()
                .map(audioClipInfo -> mono ? getAudioClipSignalDataAsMono(audioClipInfo, originalAudioFileSignal)
                                           : getAudioClipSignalDataAsStereo(audioClipInfo, originalAudioFileSignal))
                .collect(toList());
        List<float[][]> groupSeparatorSignals = Stream.generate(() -> groupSeparatorAudioFileSignal.getData())
                .limit(singleAudioClipsInfo.size())
                .collect(toList());
        return mergeClipSignalToSeparatorSignal(audioClipsAudioSignals, groupSeparatorSignals);
    }

    private float[][] getAudioClipSignalDataAsStereo(SingleAudioClipInfo singleAudioClipInfo, AudioSignal originalAudioFileSignal) {
        return getAudioClipSignalData(singleAudioClipInfo, originalAudioFileSignal, false);
    }

    private float[][] getAudioClipSignalDataAsMono(SingleAudioClipInfo singleAudioClipInfo, AudioSignal originalAudioFileSignal) {
        return getAudioClipSignalData(singleAudioClipInfo, originalAudioFileSignal, true);
    }

    private float[][] getAudioClipSignalData(SingleAudioClipInfo singleAudioClipInfo, AudioSignal originalAudioFileSignal, boolean mono) {
        int channels = mono ? MONO_CHANNELS : STEREO_CHANNELS;
        int originalAudioChannels = originalAudioFileSignal.getChannels();
        float[][] audioClipSignalData = new float[channels][];
        for (int i = 0; i < channels; i++) {
            float[] currentChannelData = originalAudioChannels < channels ? originalAudioFileSignal.getData()[0]
                    : originalAudioFileSignal.getData()[i];
            audioClipSignalData[i] = copyOfRange(currentChannelData, singleAudioClipInfo.getStartPosition(), singleAudioClipInfo.getEndPosition());
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

    private float[][] joinSeparatorToAudioClip(float[][] audioClipSignal, float[][] groupSeparatorSignal) {
        float[][] jointSignalData = new float[audioClipSignal.length][];
        for (int i = 0; i < audioClipSignal.length; i++) {
            jointSignalData[i] = ArrayUtils.addAll(audioClipSignal[i], groupSeparatorSignal[i]);
        }
        return jointSignalData;
    }

}

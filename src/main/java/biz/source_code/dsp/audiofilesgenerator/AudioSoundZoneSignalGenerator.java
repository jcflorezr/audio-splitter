package biz.source_code.dsp.audiofilesgenerator;

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.model.GroupAudioSoundZonesInfo;
import biz.source_code.dsp.model.SingleAudioSoundZoneInfo;
import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.sound.AudioIo;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.toList;

public class AudioSoundZoneSignalGenerator extends AudioSoundZoneSignalGeneratorAbstract {

    // TODO: this field should be injected with spring
    private AudioIo audioIo = new AudioIo();

    @Override
    public AudioFileWritingResult generateSingleSoundZoneAudioFile(SingleAudioSoundZoneInfo singleAudioSoundZoneInfo, String outputFileDirectoryPath, AudioSignal originalAudioFileSignal, String extension, boolean mono) {
        AudioSignal soundZoneAudioSignal = mono ? getSingleSoundZoneAudioSignalAsMono(originalAudioFileSignal)
                                                : originalAudioFileSignal;
        String outputFileName = outputFileDirectoryPath + singleAudioSoundZoneInfo.getSuggestedAudioFileName();
        int startPosition = singleAudioSoundZoneInfo.getStartPosition();
        int soundZoneLength = singleAudioSoundZoneInfo.getEndPosition() - singleAudioSoundZoneInfo.getStartPosition();
        return audioIo.saveAudioFile(outputFileName, extension, soundZoneAudioSignal, startPosition, soundZoneLength);
    }

    @Override
    public AudioFileWritingResult generateGroupSoundZonesAudioFile(GroupAudioSoundZonesInfo groupAudioSoundZonesInfo, String outputFileDirectoryPath, AudioSignal originalAudioFileSignal, AudioSignal groupSeparatorAudioFileSignal, String extension, boolean mono) {
        List<float[][]> soundZonesSignalsWithSeparator = getSoundZonesSignalsWithSeparator(groupAudioSoundZonesInfo,
                originalAudioFileSignal,
                groupSeparatorAudioFileSignal,
                mono);
        float[][] groupAudioSignalData = soundZonesSignalsWithSeparator.stream()
                .reduce((sound, sound2) -> joinSoundZoneWithSeparator(sound, sound2))
                .orElse(new float[][]{});
        int audioSamplingRate = originalAudioFileSignal.getSamplingRate();
        String groupAudioFileNameAndPath = outputFileDirectoryPath + groupAudioSoundZonesInfo.getSuggestedAudioFileName();
        AudioSignal groupAudioSignal = new AudioSignal(audioSamplingRate, groupAudioSignalData);
        return audioIo.saveAudioFile(groupAudioFileNameAndPath, extension, groupAudioSignal);
    }

    private List<float[][]> getSoundZonesSignalsWithSeparator(GroupAudioSoundZonesInfo groupAudioSoundZonesInfo, AudioSignal originalAudioFileSignal, AudioSignal groupSeparatorAudioFileSignal, boolean mono) {
        List<SingleAudioSoundZoneInfo> singleAudioSoundZonesInfo = groupAudioSoundZonesInfo.getSingleAudioSoundZonesInfo();
        List<float[][]> soundZonesAudioSignals = singleAudioSoundZonesInfo.stream()
                .map(audioSoundZoneInfo -> mono ? getSoundZoneAudioSignalDataAsMono(audioSoundZoneInfo, originalAudioFileSignal)
                                                : getSoundZoneAudioSignalDataAsStereo(audioSoundZoneInfo, originalAudioFileSignal))
                .collect(toList());
        List<float[][]> groupSeparatorSignals = Stream.generate(() -> groupSeparatorAudioFileSignal.getData())
                .limit(singleAudioSoundZonesInfo.size())
                .collect(toList());
        return mergeSoundWithGroupSeparatorSignals(soundZonesAudioSignals, groupSeparatorSignals);
    }

    private AudioSignal getSingleSoundZoneAudioSignalAsMono(AudioSignal originalAudioFileSignal) {
        return new AudioSignal(originalAudioFileSignal.getSamplingRate(), new float[][]{originalAudioFileSignal.getData()[0]});
    }

    private float[][] getSoundZoneAudioSignalDataAsStereo(SingleAudioSoundZoneInfo singleAudioSoundZoneInfo, AudioSignal originalAudioFileSignal) {
        return getSoundZoneAudioSignalData(singleAudioSoundZoneInfo, originalAudioFileSignal, false);
    }

    private float[][] getSoundZoneAudioSignalDataAsMono(SingleAudioSoundZoneInfo singleAudioSoundZoneInfo, AudioSignal originalAudioFileSignal) {
        return getSoundZoneAudioSignalData(singleAudioSoundZoneInfo, originalAudioFileSignal, true);
    }

    private float[][] getSoundZoneAudioSignalData(SingleAudioSoundZoneInfo singleAudioSoundZoneInfo, AudioSignal originalAudioFileSignal, boolean mono) {
        int channels = mono ? 1 : originalAudioFileSignal.getChannels();
        float[][] soundZoneSignalData = new float[channels][];
        for (int i = 0; i < channels; i++) {
            int startPosition = singleAudioSoundZoneInfo.getStartPosition();
            int endPosition = singleAudioSoundZoneInfo.getEndPosition();
            soundZoneSignalData[i] = copyOfRange(originalAudioFileSignal.getData()[i], startPosition, endPosition);
        }
        return soundZoneSignalData;
    }

    private List<float[][]> mergeSoundWithGroupSeparatorSignals(List<float[][]> soundZonesAudioSignals, List<float[][]> groupSeparatorSignals) {
        List<float[][]> soundZonesSignalsWithSeparator = new ArrayList<>();
        int soundZonesAudioSignalsSize = soundZonesAudioSignals.size();
        int groupSeparatorSignalsSize = groupSeparatorSignals.size();
        for (int i = 0; i < soundZonesAudioSignalsSize; i++) {
            soundZonesSignalsWithSeparator.add(soundZonesAudioSignals.get(i));
            if (i < groupSeparatorSignalsSize) {
                soundZonesSignalsWithSeparator.add(groupSeparatorSignals.get(i));
            }
        }
        return soundZonesSignalsWithSeparator;
    }

    private float[][] joinSoundZoneWithSeparator(float[][] soundZoneSignal, float[][] groupSeparatorSignal) {
        float[][] jointSignalData = new float[soundZoneSignal.length][];
        for (int i = 0; i < soundZoneSignal.length; i++) {
            jointSignalData[i] = ArrayUtils.addAll(soundZoneSignal[i], groupSeparatorSignal[i]);
        }
        return jointSignalData;
    }

}

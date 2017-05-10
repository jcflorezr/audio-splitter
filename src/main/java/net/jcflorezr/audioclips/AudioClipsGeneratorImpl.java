package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AudioClipsGeneratorImpl implements AudioClipsGenerator {

    private GroupAudioClipSignalGenerator groupAudioClipSignalGenerator = new GroupAudioClipSignalGenerator();
    private SingleAudioClipSignalGenerator singleAudioClipSignalGenerator = new SingleAudioClipSignalGenerator();
    private AudioIo audioIo = new AudioIo();

    @Override
    public List<AudioClipsWritingResult> generateAudioClip(AudioFileInfo audioFileInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup) {
        List<AudioClipInfo> audioClipsInfo = audioFileInfo.getAudioClipsInfo();
        if (generateAudioClipsByGroup) {
            return audioClipsInfo.stream()
                    .collect(Collectors.groupingBy(AudioClipInfo::getGroupNumber))
                    .entrySet().stream()
                    .map(audioClipsGroupInfo -> generateAudioClipsByGroup(audioClipsGroupInfo.getValue(), outputAudioClipsConfig))
                    .collect(Collectors.toList());
        } else {
            return audioClipsInfo.stream()
                    .map(audioClipInfo -> generateSingleAudioClips(audioClipInfo, outputAudioClipsConfig))
                    .collect(Collectors.toList());
        }
    }

    private AudioClipsWritingResult generateAudioClipsByGroup(List<AudioClipInfo> audioClipsGroupInfo, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioSignal audioClipSignal = groupAudioClipSignalGenerator.generateAudioClip(audioClipsGroupInfo, outputAudioClipsConfig);
        String suggestedAudioClipName = audioClipsGroupInfo.get(0).getSuggestedAudioClipName();
        String groupAudioFileNameAndPath = outputAudioClipsConfig.getOutputAudioClipsDirectoryPath() + suggestedAudioClipName;
        AudioFileWritingResult audioFileWritingResult = audioIo.saveAudioFile(groupAudioFileNameAndPath, outputAudioClipsConfig.getAudioFormatExtension(), audioClipSignal);
        return new AudioClipsWritingResult(suggestedAudioClipName, audioFileWritingResult);
    }

    private AudioClipsWritingResult generateSingleAudioClips(AudioClipInfo audioClipInfo, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioSignal audioClipSignal = singleAudioClipSignalGenerator.generateAudioClip(audioClipInfo, outputAudioClipsConfig);
        String suggestedAudioClipName = audioClipInfo.getSuggestedAudioClipName();
        String outputFileName = outputAudioClipsConfig.getOutputAudioClipsDirectoryPath() + suggestedAudioClipName;
        int startPosition = audioClipInfo.getStartPosition();
        int audioClipLength = audioClipInfo.getEndPosition() - startPosition;
        AudioFileWritingResult audioFileWritingResult = audioIo.saveAudioFile(outputFileName, outputAudioClipsConfig.getAudioFormatExtension(), audioClipSignal, startPosition, audioClipLength);
        return new AudioClipsWritingResult(suggestedAudioClipName, audioFileWritingResult);
    }

}

package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.model.audioclips.AudioFileClipEntity;
import net.jcflorezr.model.audioclips.AudioFileClipResultEntity;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AudioClipsGeneratorImpl implements AudioClipsGenerator {

    @Autowired
    private GroupAudioClipSignalGenerator groupAudioClipSignalGenerator;

    @Autowired
    private SingleAudioClipSignalGenerator singleAudioClipSignalGenerator;

    @Autowired
    private AudioIo audioIo;

    @Override
    public List<AudioFileClipResultEntity> generateAudioClip(String audioFileName, AudioFileCompleteInfo audioFileCompleteInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup) {
        List<AudioFileClipEntity> audioClipsInfo = audioFileCompleteInfo.getAudioClipsInfo();
        if (generateAudioClipsByGroup) {
            return audioClipsInfo.stream()
                    .collect(Collectors.groupingBy(AudioFileClipEntity::getGroupNumber))
                    .entrySet().stream()
                    .map(audioClipsGroupInfo -> generateAudioClipsByGroup(audioFileName, audioClipsGroupInfo.getValue(), outputAudioClipsConfig))
                    .collect(Collectors.toList());
        } else {
            return audioClipsInfo.stream()
                    .map(audioClipInfo -> generateSingleAudioClips(audioFileName, audioClipInfo, outputAudioClipsConfig))
                    .collect(Collectors.toList());
        }
    }

    private AudioFileClipResultEntity generateAudioClipsByGroup(String audioFileName, List<AudioFileClipEntity> audioClipsGroupInfo, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioSignal audioClipSignal = groupAudioClipSignalGenerator.generateAudioClip(audioClipsGroupInfo, outputAudioClipsConfig);
        String suggestedAudioClipName = audioClipsGroupInfo.get(0).getAudioClipName();
        String groupAudioFileNameAndPath = outputAudioClipsConfig.getOutputAudioClipsDirectoryPath() + suggestedAudioClipName;
        AudioFileWritingResult audioFileWritingResult = audioIo.saveAudioFile(groupAudioFileNameAndPath, outputAudioClipsConfig.getAudioFormatExtension(), audioClipSignal);
        return new AudioFileClipResultEntity(audioFileName, audioClipsGroupInfo.get(0), audioFileWritingResult, suggestedAudioClipName);
    }

    private AudioFileClipResultEntity generateSingleAudioClips(String audioFileName, AudioFileClipEntity audioFileClipEntity, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioSignal audioClipSignal = singleAudioClipSignalGenerator.generateAudioClip(audioFileClipEntity, outputAudioClipsConfig);
        String suggestedAudioClipName = audioFileClipEntity.getAudioClipName();
        String audioFileNameAndPath = outputAudioClipsConfig.getOutputAudioClipsDirectoryPath() + suggestedAudioClipName;
        int startPosition = audioFileClipEntity.getStartPosition();
        int audioClipLength = audioFileClipEntity.getEndPosition() - startPosition;
        AudioFileWritingResult audioFileWritingResult = audioIo.saveAudioFile(audioFileNameAndPath, outputAudioClipsConfig.getAudioFormatExtension(), audioClipSignal, startPosition, audioClipLength);
        return new AudioFileClipResultEntity(audioFileName, audioFileClipEntity, audioFileWritingResult, suggestedAudioClipName);
    }

}

package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.model.audioclips.AudioFileClip;
import net.jcflorezr.model.audioclips.AudioFileClipResult;
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
    public List<AudioFileClipResult> generateAudioClip(String audioFileName, AudioFileCompleteInfo audioFileCompleteInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup) {
        List<AudioFileClip> audioClipsInfo = audioFileCompleteInfo.getAudioClipsInfo();
        if (generateAudioClipsByGroup) {
            return audioClipsInfo.stream()
                    .collect(Collectors.groupingBy(AudioFileClip::getGroupNumber))
                    .entrySet().stream()
                    .map(audioClipsGroupInfo -> generateAudioClipsByGroup(audioFileName, audioClipsGroupInfo.getValue(), outputAudioClipsConfig))
                    .collect(Collectors.toList());
        } else {
            return audioClipsInfo.stream()
                    .map(audioClipInfo -> generateSingleAudioClips(audioFileName, audioClipInfo, outputAudioClipsConfig))
                    .collect(Collectors.toList());
        }
    }

    private AudioFileClipResult generateAudioClipsByGroup(String audioFileName, List<AudioFileClip> audioClipsGroupInfo, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioSignal audioClipSignal = groupAudioClipSignalGenerator.generateAudioClip(audioClipsGroupInfo, outputAudioClipsConfig);
        String suggestedAudioClipName = audioClipsGroupInfo.get(0).getAudioClipName();
        String groupAudioFileNameAndPath = outputAudioClipsConfig.getOutputAudioClipsDirectoryPath() + suggestedAudioClipName;
        AudioFileWritingResult audioFileWritingResult = audioIo.saveAudioFile(groupAudioFileNameAndPath, outputAudioClipsConfig.getAudioFormatExtension(), audioClipSignal);
        return new AudioFileClipResult(audioFileName, audioClipsGroupInfo.get(0), audioFileWritingResult, suggestedAudioClipName);
    }

    private AudioFileClipResult generateSingleAudioClips(String audioFileName, AudioFileClip audioFileClip, OutputAudioClipsConfig outputAudioClipsConfig) {
        AudioSignal audioClipSignal = singleAudioClipSignalGenerator.generateAudioClip(audioFileClip, outputAudioClipsConfig);
        String suggestedAudioClipName = audioFileClip.getAudioClipName();
        String audioFileNameAndPath = outputAudioClipsConfig.getOutputAudioClipsDirectoryPath() + suggestedAudioClipName;
        int startPosition = audioFileClip.getStartPosition();
        int audioClipLength = audioFileClip.getEndPosition() - startPosition;
        AudioFileWritingResult audioFileWritingResult = audioIo.saveAudioFile(audioFileNameAndPath, outputAudioClipsConfig.getAudioFormatExtension(), audioClipSignal, startPosition, audioClipLength);
        return new AudioFileClipResult(audioFileName, audioFileClip, audioFileWritingResult, suggestedAudioClipName);
    }

}

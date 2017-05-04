package net.jcflorezr.audioclips;

import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioFileInfo;

import java.util.List;
import java.util.stream.Collectors;

public class AudioClipsGeneratorImpl implements AudioClipsGenerator {

    private GroupAudioClipGenerator groupAudioClipGenerator = new GroupAudioClipGenerator();
    private SingleAudioClipGenerator singleAudioClipGenerator = new SingleAudioClipGenerator();

    @Override
    public List<AudioClipsWritingResult> generateAudioClip(AudioFileInfo audioFileInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup) {
        List<AudioClipInfo> audioClipsInfo = audioFileInfo.getAudioClipsInfo();
        if (generateAudioClipsByGroup) {
            return audioClipsInfo.stream()
                    .collect(Collectors.groupingBy(AudioClipInfo::getGroupNumber))
                    .entrySet().stream()
                    .map(audioClipsGroupInfo ->
                            new AudioClipsWritingResult(audioClipsGroupInfo.getValue().get(0).getSuggestedAudioClipName(),
                                    groupAudioClipGenerator.generateAudioClip(audioClipsGroupInfo.getValue(), outputAudioClipsConfig)))
                    .collect(Collectors.toList());
        } else {
            return audioClipsInfo.stream()
                    .map(audioClipInfo ->
                        new AudioClipsWritingResult(audioClipInfo.getSuggestedAudioClipName(),
                            singleAudioClipGenerator.generateAudioClip(audioClipInfo, outputAudioClipsConfig)))
                    .collect(Collectors.toList());
        }
    }

}

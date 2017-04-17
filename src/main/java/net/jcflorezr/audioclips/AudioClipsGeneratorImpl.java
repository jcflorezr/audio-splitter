package net.jcflorezr.audioclips;

import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audioclips.SingleAudioClipInfo;
import net.jcflorezr.model.audiocontent.AudioFileInfo;

import java.util.List;
import java.util.stream.Collectors;

public class AudioClipsGeneratorImpl implements AudioClipsGenerator {

    private GroupAudioClipGenerator groupAudioClipGenerator = new GroupAudioClipGenerator();
    private SingleAudioClipGenerator singleAudioClipGenerator = new SingleAudioClipGenerator();




    @Override
    public List<AudioClipsWritingResult> generateAudioClip(AudioFileInfo audioFileInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup) {

//        List<? extends AudioClipInfo> audioClipsInfo =
//                generateAudioClipsByGroup ? audioFileInfo.getAudioClipsWritingResult() : audioFileInfo.getSingleAudioClipsInfo();
//        audioClipsInfo.stream().forEach(clip ->
//                clip.setAudioClipWritingResult(audioClipsGenerator.generateAudioClip(clip, outputAudioClipsConfig, generateAudioClipsByGroup)));

        List<SingleAudioClipInfo> audioClipsInfo = audioFileInfo.getSingleAudioClipsInfo();
        if (generateAudioClipsByGroup) {
            return audioClipsInfo.stream()
                    .collect(Collectors.groupingBy(SingleAudioClipInfo::getGroupNumber))
                    .entrySet().stream()
                    .map(audioClipsGroup ->
                            new AudioClipsWritingResult(audioClipsGroup.getValue().get(0).getSuggestedAudioClipName(),
                                    groupAudioClipGenerator.generateAudioClip(audioClipsGroup.getValue(), outputAudioClipsConfig)))
                    .collect(Collectors.toList());
        } else {
            return audioClipsInfo.stream()
                    .map(clip ->
                        new AudioClipsWritingResult(clip.getSuggestedAudioClipName(),
                            singleAudioClipGenerator.generateAudioClip(clip, outputAudioClipsConfig)))
                    .collect(Collectors.toList());
        }
    }

}

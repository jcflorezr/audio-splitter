package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioFileWritingResult;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.audioclips.GroupAudioClipInfo;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audioclips.SingleAudioClipInfo;

public class AudioClipsGeneratorImpl implements AudioClipsGenerator {

    private GroupAudioClipGenerator groupAudioClipGenerator = new GroupAudioClipGenerator();
    private SingleAudioClipGenerator singleAudioClipGenerator = new SingleAudioClipGenerator();

    @Override
    public AudioFileWritingResult generateAudioClip(AudioClipInfo audioClipInfo, OutputAudioClipsConfig outputAudioClipsConfig, boolean generateAudioClipsByGroup) {
        if (generateAudioClipsByGroup) {
            return groupAudioClipGenerator.generateAudioClip((GroupAudioClipInfo) audioClipInfo, outputAudioClipsConfig);
        } else {
            return singleAudioClipGenerator.generateAudioClip((SingleAudioClipInfo) audioClipInfo, outputAudioClipsConfig);
        }
    }

}

package net.jcflorezr.endpoint;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.api.endpoint.AudioSplitterByGroup;
import net.jcflorezr.model.request.AudioFileLocation;
import net.jcflorezr.model.response.AudioSplitterResponse;

public class FlacAudioSplitterByGroup extends AudioSplitterByGroup {

    private final AudioFormatsSupported audioFormat = AudioFormatsSupported.FLAC;
    private final boolean generateAudioClipsByGroup = true;

    @Override
    public AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation) {
        boolean asMono = false;
        boolean withSeparator = false;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }
    @Override
    public AudioSplitterResponse generateAudioClipsWithSeparator(AudioFileLocation audioFileLocation) {
        boolean asMono = false;
        boolean withSeparator = true;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }

    @Override
    public AudioSplitterResponse generateAudioMonoClips(AudioFileLocation audioFileLocation) {
        boolean asMono = true;
        boolean withSeparator = false;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }

    @Override
    public AudioSplitterResponse generateAudioMonoClipsWithSeparator(AudioFileLocation audioFileLocation) {
        boolean asMono = true;
        boolean withSeparator = true;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }

}

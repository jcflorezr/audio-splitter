package net.jcflorezr.endpoint;

import net.jcflorezr.api.endpoint.AudioSplitter;
import net.jcflorezr.model.response.AudioSplitterResponse;
import net.jcflorezr.model.request.AudioFileLocation;
import biz.source_code.dsp.util.AudioFormatsSupported;

public class WavAudioSplitterByGroup extends AudioSplitter {

    private final AudioFormatsSupported audioFormat = AudioFormatsSupported.WAV;
    private final boolean generateAudioClipsByGroup = true;

    @Override
    public AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation) {
        boolean asMono = false;
        boolean withSeparator = true;
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

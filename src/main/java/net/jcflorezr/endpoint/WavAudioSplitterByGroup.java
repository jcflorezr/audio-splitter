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
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup);
    }

    @Override
    public AudioSplitterResponse generateAudioMonoClips(AudioFileLocation audioFileLocation) {
        boolean asMono = true;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup);
    }
}

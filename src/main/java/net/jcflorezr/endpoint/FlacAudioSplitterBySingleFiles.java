package net.jcflorezr.endpoint;

import net.jcflorezr.api.endpoint.AudioSplitter;
import net.jcflorezr.model.response.AudioSplitterResponse;
import net.jcflorezr.model.request.AudioFileLocation;
import biz.source_code.dsp.util.AudioFormatsSupported;

public class FlacAudioSplitterBySingleFiles extends AudioSplitter {

    private final AudioFormatsSupported audioFormat = AudioFormatsSupported.FLAC;

    @Override
    public AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation) {
        boolean asMono = false;
        return generateAudioClips(audioFileLocation, audioFormat, asMono);
    }

    @Override
    public AudioSplitterResponse generateAudioMonoClips(AudioFileLocation audioFileLocation) {
        boolean asMono = true;
        return generateAudioClips(audioFileLocation, audioFormat, asMono);
    }

}

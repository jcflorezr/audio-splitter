package biz.source_code.dsp.endpoint;

import biz.source_code.dsp.api.endpoint.AudioSplitter;
import biz.source_code.dsp.api.model.response.AudioSplitterResponse;
import biz.source_code.dsp.model.AudioFileLocation;
import biz.source_code.dsp.util.AudioFormatsSupported;

public class FlacAudioSplitterByGroup extends AudioSplitter {

    private final AudioFormatsSupported audioFormat = AudioFormatsSupported.FLAC;

    @Override
    public AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation) {
        boolean asMono = false;
        return generateAudioClipsByGroup(audioFileLocation, audioFormat, asMono);
    }

    @Override
    public AudioSplitterResponse generateAudioMonoClips(AudioFileLocation audioFileLocation) {
        boolean asMono = true;
        return generateAudioClipsByGroup(audioFileLocation, audioFormat, asMono);
    }

}

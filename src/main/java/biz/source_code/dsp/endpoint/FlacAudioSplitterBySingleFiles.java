package biz.source_code.dsp.endpoint;

import biz.source_code.dsp.api.endpoint.AudioSplitter;
import biz.source_code.dsp.api.model.response.AudioSplitterResponse;
import biz.source_code.dsp.model.AudioFileLocation;
import biz.source_code.dsp.util.AudioFormatsSupported;

public class FlacAudioSplitterBySingleFiles extends AudioSplitter {

    private final AudioFormatsSupported audioFormat = AudioFormatsSupported.FLAC;
    private final boolean generateAudioClipsByGroup = false;

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

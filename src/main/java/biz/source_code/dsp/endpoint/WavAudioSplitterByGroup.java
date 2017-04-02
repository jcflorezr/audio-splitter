package biz.source_code.dsp.endpoint;

import biz.source_code.dsp.api.endpoint.AudioSplitter;
import biz.source_code.dsp.api.model.response.AudioSplitterResponse;
import biz.source_code.dsp.model.AudioFileLocation;
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

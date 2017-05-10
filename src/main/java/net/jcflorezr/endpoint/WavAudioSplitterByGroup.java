package net.jcflorezr.endpoint;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.api.endpoint.AudioSplitterByGroup;
import net.jcflorezr.model.request.AudioFileLocation;
import net.jcflorezr.model.response.AudioSplitterResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wav/by-group")
public class WavAudioSplitterByGroup extends AudioSplitterByGroup {

    private final AudioFormatsSupported audioFormat = AudioFormatsSupported.WAV;
    private final boolean generateAudioClipsByGroup = true;

    @Override
    @PostMapping(value = "/generate-audio-clips", consumes = "application/json", produces = "application/json")
    public AudioSplitterResponse generateAudioClips(AudioFileLocation audioFileLocation) {
        boolean asMono = false;
        boolean withSeparator = true;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }

    @Override
    @PostMapping(value = "/generate-audio-clips-with-separator", consumes = "application/json", produces = "application/json")
    public AudioSplitterResponse generateAudioClipsWithSeparator(AudioFileLocation audioFileLocation) {
        boolean asMono = false;
        boolean withSeparator = true;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }

    @Override
    @PostMapping(value = "/generate-audio-mono-clips", consumes = "application/json", produces = "application/json")
    public AudioSplitterResponse generateAudioMonoClips(AudioFileLocation audioFileLocation) {
        boolean asMono = true;
        boolean withSeparator = false;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }

    @Override
    @PostMapping(value = "/generate-audio-mono-clips-with-separator", consumes = "application/json", produces = "application/json")
    public AudioSplitterResponse generateAudioMonoClipsWithSeparator(AudioFileLocation audioFileLocation) {
        boolean asMono = true;
        boolean withSeparator = true;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }
}

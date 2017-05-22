package net.jcflorezr.endpoint;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.api.endpoint.AudioSplitterByGroup;
import net.jcflorezr.model.request.AudioFileLocation;
import net.jcflorezr.model.response.AudioSplitterResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/flac/by-group")
public class FlacAudioSplitterByGroup extends AudioSplitterByGroup {

    private final AudioFormatsSupported audioFormat = AudioFormatsSupported.FLAC;
    private final boolean generateAudioClipsByGroup = true;

    @Override
    @PostMapping(value = "/generate-audio-clips", consumes = "application/json", produces = "application/json")
    public AudioSplitterResponse generateAudioClips(@RequestBody AudioFileLocation audioFileLocation) {
        boolean asMono = false;
        boolean withSeparator = false;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }

    @Override
    @PostMapping(value = "generate-audio-clips-with-separator", consumes = "application/json", produces = "application/json")
    public AudioSplitterResponse generateAudioClipsWithSeparator(@RequestBody AudioFileLocation audioFileLocation) {
        boolean asMono = false;
        boolean withSeparator = true;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }

    @Override
    @PostMapping(value = "generate-audio-mono-clips", consumes = "application/json", produces = "application/json")
    public AudioSplitterResponse generateAudioMonoClips(@RequestBody AudioFileLocation audioFileLocation) {
        boolean asMono = true;
        boolean withSeparator = false;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }

    @Override
    @PostMapping(value = "generate-audio-mono-clips-with-separator", consumes = "application/json", produces = "application/json")
    public AudioSplitterResponse generateAudioMonoClipsWithSeparator(@RequestBody AudioFileLocation audioFileLocation) {
        boolean asMono = true;
        boolean withSeparator = true;
        return generateAudioClips(audioFileLocation, audioFormat, asMono, generateAudioClipsByGroup, withSeparator);
    }

}

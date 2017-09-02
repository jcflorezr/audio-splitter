package net.jcflorezr.endpoint;

import net.jcflorezr.api.endpoint.AudioSplitter;
import net.jcflorezr.model.request.AudioFileBasicInfo;
import net.jcflorezr.model.response.AudioSplitterResponse;
import biz.source_code.dsp.util.AudioFormatsSupported;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wav")
public class WavAudioSplitterBySingleFiles extends AudioSplitter {

    private final AudioFormatsSupported audioFormat = AudioFormatsSupported.WAV;

    @Override
    @PostMapping("/generate-audio-clips")
    public AudioSplitterResponse generateAudioClips(@RequestBody AudioFileBasicInfo audioFileBasicInfo) {
        boolean asMono = false;
        return generateAudioClips(audioFileBasicInfo, audioFormat, asMono);
    }

    @Override
    @PostMapping("/generate-audio-mono-clips")
    public AudioSplitterResponse generateAudioMonoClips(@RequestBody AudioFileBasicInfo audioFileBasicInfo) {
        boolean asMono = true;
        return generateAudioClips(audioFileBasicInfo, audioFormat, asMono);
    }
}

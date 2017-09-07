package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioSignal;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.model.audioclips.AudioFileClipEntity;
import net.jcflorezr.model.audioclips.OutputAudioClipsConfig;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileMetadataEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SingleAudioClipSignalGeneratorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String AUDIO_CLIP_INFO = "/audioclips/audio-clip-info-min.json";
    private static final String AUDIO_SIGNAL = "/audioclips/audio-signal-min.json";
    private static final String SINGLE_MONO_SIGNAL = "/audioclips/single-mono-signal.json";
    private static final String SINGLE_STEREO_SIGNAL = "/audioclips/single-stereo-signal.json";
    private static final int DEFAULT_SAMPLING_RATE = 22;

    private Class<? extends SingleAudioClipSignalGeneratorTest> thisClass;

    @InjectMocks
    private SingleAudioClipSignalGenerator singleAudioClipSignalGenerator;

    @Before
    public void setUp() {
        thisClass = this.getClass();
    }

    @Test
    public void generateSingleAudioMonoSignal() throws Exception {
        AudioFileClipEntity singleAudioFileClipEntity = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_CLIP_INFO), AudioFileClipEntity.class);
        boolean asMono = true;
        OutputAudioClipsConfig outputAudioClipsConfig = createDummyOutputAudioClipsConfig(asMono);

        AudioSignal actualAudioClipSignal = singleAudioClipSignalGenerator.generateAudioClip(singleAudioFileClipEntity, outputAudioClipsConfig);
        AudioSignal expectedAudioClipSignal = MAPPER.readValue(thisClass.getResourceAsStream(SINGLE_MONO_SIGNAL), AudioSignal.class);

        assertThat(actualAudioClipSignal, is(expectedAudioClipSignal));
    }

    @Test
    public void generateSingleAudioStereoSignal() throws Exception {
        AudioFileClipEntity singleAudioFileClipEntity = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_CLIP_INFO), AudioFileClipEntity.class);
        boolean asMono = false;
        OutputAudioClipsConfig outputAudioClipsConfig = createDummyOutputAudioClipsConfig(asMono);

        AudioSignal actualAudioClipSignal = singleAudioClipSignalGenerator.generateAudioClip(singleAudioFileClipEntity, outputAudioClipsConfig);
        AudioSignal expectedAudioClipSignal = MAPPER.readValue(thisClass.getResourceAsStream(SINGLE_STEREO_SIGNAL), AudioSignal.class);

        assertThat(actualAudioClipSignal, is(expectedAudioClipSignal));
    }

    private OutputAudioClipsConfig createDummyOutputAudioClipsConfig(boolean asMono) throws IOException {
        String outputAudioClipsDirectoryPath = "any-directory-path";

        int samplingRate = DEFAULT_SAMPLING_RATE;
        float[][] audioSignalData = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_SIGNAL), float[][].class);
        AudioSignal dummyAudioSignal = new AudioSignal(samplingRate, audioSignalData);
        AudioFileMetadataEntity dummyAudioFileMetadataEntity = new AudioFileMetadataEntity();
        AudioContent dummyAudioContent = new AudioContent(dummyAudioSignal, dummyAudioFileMetadataEntity);

        String audioFormatExtension = "any-file-extension";

        return new OutputAudioClipsConfig(outputAudioClipsDirectoryPath, dummyAudioContent, audioFormatExtension, asMono, false);
    }

}
package net.jcflorezr.audioclips;

import biz.source_code.dsp.model.AudioSignal;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GroupAudioClipSignalGeneratorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String AUDIO_CLIPS_INFO = "/audioclips/audio-clips-info-min.json";
    private static final String AUDIO_SIGNAL = "/audioclips/audio-signal-min.json";
    private static final int DEFAULT_SAMPLING_RATE = 22;
    private static final String MONO_SIGNAL_WITHOUT_SEPARATOR = "/audioclips/audio-mono-signal-without-separator.json";
    private static final String MONO_SIGNAL_WITH_SEPARATOR = "/audioclips/audio-mono-signal-with-separator.json";
    private static final String STEREO_SIGNAL_WITHOUT_SEPARATOR = "/audioclips/audio-stereo-signal-without-separator.json";
    private static final String STEREO_SIGNAL_WITH_SEPARATOR = "/audioclips/audio-stereo-signal-with-separator.json";

    private Class<? extends GroupAudioClipSignalGeneratorTest> thisClass;

    @InjectMocks
    private GroupAudioClipSignalGenerator groupAudioClipSignalGenerator;

    @Before
    public void setUp() {
        thisClass = this.getClass();
    }

    @Test
    public void generateAudioMonoSignalWithoutSeparator() throws Exception {
        List<AudioFileClipEntity> groupAudioClipsInfo = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_CLIPS_INFO), new TypeReference<List<AudioFileClipEntity>>(){});
        boolean asMono = true;
        boolean withSeparator = false;
        OutputAudioClipsConfig outputAudioClipsConfig = createDummyOutputAudioClipsConfig(asMono, withSeparator);

        AudioSignal actualAudioClipSignal = groupAudioClipSignalGenerator.generateAudioClip(groupAudioClipsInfo, outputAudioClipsConfig);
        AudioSignal expectedAudioClipSignal = MAPPER.readValue(thisClass.getResourceAsStream(MONO_SIGNAL_WITHOUT_SEPARATOR), AudioSignal.class);

        assertThat(actualAudioClipSignal, is(expectedAudioClipSignal));
    }

    @Test
    public void generateAudioMonoSignalWithSeparator() throws Exception {
        List<AudioFileClipEntity> groupAudioClipsInfo = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_CLIPS_INFO), new TypeReference<List<AudioFileClipEntity>>(){});
        boolean asMono = true;
        boolean withSeparator = true;
        OutputAudioClipsConfig outputAudioClipsConfig = createDummyOutputAudioClipsConfig(asMono, withSeparator);

        AudioSignal actualAudioClipSignal = groupAudioClipSignalGenerator.generateAudioClip(groupAudioClipsInfo, outputAudioClipsConfig);
        AudioSignal expectedAudioClipSignal = MAPPER.readValue(thisClass.getResourceAsStream(MONO_SIGNAL_WITH_SEPARATOR), AudioSignal.class);

        assertThat(actualAudioClipSignal, is(expectedAudioClipSignal));
    }

    @Test
    public void generateAudioStereoSignalWithoutSeparator() throws Exception {
        List<AudioFileClipEntity> groupAudioClipsInfo = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_CLIPS_INFO), new TypeReference<List<AudioFileClipEntity>>(){});
        boolean asMono = false;
        boolean withSeparator = false;
        OutputAudioClipsConfig outputAudioClipsConfig = createDummyOutputAudioClipsConfig(asMono, withSeparator);

        AudioSignal actualAudioClipSignal = groupAudioClipSignalGenerator.generateAudioClip(groupAudioClipsInfo, outputAudioClipsConfig);
        AudioSignal expectedAudioClipSignal = MAPPER.readValue(thisClass.getResourceAsStream(STEREO_SIGNAL_WITHOUT_SEPARATOR), AudioSignal.class);

        assertThat(actualAudioClipSignal, is(expectedAudioClipSignal));
    }

    @Test
    public void generateAudioStereoSignalWithSeparator() throws Exception {
        List<AudioFileClipEntity> groupAudioClipsInfo = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_CLIPS_INFO), new TypeReference<List<AudioFileClipEntity>>(){});
        boolean asMono = false;
        boolean withSeparator = true;
        OutputAudioClipsConfig outputAudioClipsConfig = createDummyOutputAudioClipsConfig(asMono, withSeparator);

        AudioSignal actualAudioClipSignal = groupAudioClipSignalGenerator.generateAudioClip(groupAudioClipsInfo, outputAudioClipsConfig);
        AudioSignal expectedAudioClipSignal = MAPPER.readValue(thisClass.getResourceAsStream(STEREO_SIGNAL_WITH_SEPARATOR), AudioSignal.class);

        assertThat(actualAudioClipSignal, is(expectedAudioClipSignal));
    }

    private OutputAudioClipsConfig createDummyOutputAudioClipsConfig(boolean asMono, boolean withSeparator) throws IOException {
        String outputAudioClipsDirectoryPath = "any-directory-path";

        int samplingRate = DEFAULT_SAMPLING_RATE;
        float[][] audioSignalData = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_SIGNAL), float[][].class);
        AudioSignal dummyAudioSignal = new AudioSignal(samplingRate, audioSignalData);
        AudioFileMetadataEntity dummyAudioFileMetadataEntity = new AudioFileMetadataEntity();
        AudioContent dummyAudioContent = new AudioContent(dummyAudioSignal, dummyAudioFileMetadataEntity);

        String audioFormatExtension = "any-file-extension";

        return new OutputAudioClipsConfig(outputAudioClipsDirectoryPath, dummyAudioContent, audioFormatExtension, asMono, withSeparator);
    }

}
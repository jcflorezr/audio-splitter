package net.jcflorezr.audiocontent.signal;

import biz.source_code.dsp.model.AudioSignal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.model.audioclips.AudioFileClipEntity;
import net.jcflorezr.model.audiocontent.signal.RmsSignalInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoundZonesDetectorImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String BACKGROUND_NOISE_LOW_VOLUME_SIGNAL = "/audiocontent/signal/background-noise-low-volume-signal.json";
    private static final String BACKGROUND_NOISE_LOW_VOLUME_RMS_INFO = "/audiocontent/signal/background-noise-low-volume-rms-info.json";
    private static final String BACKGROUND_NOISE_LOW_VOLUME_AUDIO_CLIPS_INFO = "/audiocontent/signal/background-noise-low-volume-audio-clips-info.json";
    private static final String WITH_APPLAUSE_SIGNAL = "/audiocontent/signal/with-applause-signal.json";
    private static final String WITH_APPLAUSE_RMS_INFO = "/audiocontent/signal/with-applause-rms-info.json";
    private static final String WITH_APPLAUSE_AUDIO_CLIPS_INFO = "/audiocontent/signal/with-applause-audio-clips-info.json";
    private static final String STRONG_BACKGROUND_NOISE_SIGNAL = "/audiocontent/signal/strong-background-noise-signal.json";
    private static final String STRONG_BACKGROUND_NOISE_RMS_INFO = "/audiocontent/signal/strong-background-noise-rms-info.json";
    private static final String STRONG_BACKGROUND_NOISE_AUDIO_CLIPS_INFO = "/audiocontent/signal/strong-background-noise-audio-clips-info.json";

    private Class<? extends SoundZonesDetectorImplTest> thisClass;

    @Mock
    private RmsCalculator rmsCalculator;
    @InjectMocks
    private SoundZonesDetectorImpl soundZonesDetector;

    @Before
    public void setUp() throws Exception {
        thisClass = this.getClass();
    }

    @Test
    public void retrieveAudioClipsWithBackgroundNoiseAndLowVolume() throws Exception {
        testAudioClipsProcess(BACKGROUND_NOISE_LOW_VOLUME_RMS_INFO,
                              BACKGROUND_NOISE_LOW_VOLUME_SIGNAL,
                              BACKGROUND_NOISE_LOW_VOLUME_AUDIO_CLIPS_INFO);
    }

    @Test
    public void retrieveAudioClipsWithApplause() throws Exception {
        testAudioClipsProcess(WITH_APPLAUSE_RMS_INFO,
                              WITH_APPLAUSE_SIGNAL,
                              WITH_APPLAUSE_AUDIO_CLIPS_INFO);
    }

    @Test
    public void retrieveAudioClipsWithStrongBackgroundNoise() throws Exception {
        testAudioClipsProcess(STRONG_BACKGROUND_NOISE_RMS_INFO,
                              STRONG_BACKGROUND_NOISE_SIGNAL,
                              STRONG_BACKGROUND_NOISE_AUDIO_CLIPS_INFO);
    }

    private void testAudioClipsProcess(String rmsSignalInfoFileName, String audioSignalFileName, String expectedAudioClipsFileName) throws Exception {
        List<RmsSignalInfo> rmsSignalInfo = MAPPER.readValue(thisClass.getResourceAsStream(rmsSignalInfoFileName), new TypeReference<List<RmsSignalInfo>>() {});
        when(rmsCalculator.retrieveRmsInfo(anyObject(), anyInt(), anyInt())).thenReturn(rmsSignalInfo);

        String audioFileName = "/path/to-find/audio-file";
        AudioSignal withApplauseSignal = MAPPER.readValue(thisClass.getResourceAsStream(audioSignalFileName), AudioSignal.class);
        List<AudioFileClipEntity> actualAudioClips = soundZonesDetector.retrieveAudioClipsInfo(audioFileName, withApplauseSignal);
        List<AudioFileClipEntity> expectedAudioClips = MAPPER.readValue(thisClass.getResourceAsStream(expectedAudioClipsFileName), new TypeReference<List<AudioFileClipEntity>>() {});

        assertThat(actualAudioClips, equalTo(expectedAudioClips));
    }

}
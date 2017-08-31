package net.jcflorezr.audiofileinfo.signal;

import biz.source_code.dsp.model.AudioSignal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.audiocontent.signal.RmsSignalInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoundZonesDetectorImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String BACKGROUND_NOISE_LOW_VOLUME_SIGNAL = "/audiofileinfo/signal/background-noise-low-volume-signal.json";
    private static final String BACKGROUND_NOISE_LOW_VOLUME_RMS_INFO = "/audiofileinfo/signal/background-noise-low-volume-rms-info.json";
    private static final String BACKGROUND_NOISE_LOW_VOLUME_AUDIO_CLIPS_INFO = "/audiofileinfo/signal/background-noise-low-volume-audio-clips-info.json";
    private static final String WITH_APPLAUSE_SIGNAL = "/audiofileinfo/signal/with-applause-signal.json";
    private static final String WITH_APPLAUSE_RMS_INFO = "/audiofileinfo/signal/with-applause-rms-info.json";
    private static final String WITH_APPLAUSE_AUDIO_CLIPS_INFO = "/audiofileinfo/signal/with-applause-audio-clips-info.json";
    private static final String STRONG_BACKGROUND_NOISE_SIGNAL = "/audiofileinfo/signal/strong-background-noise-signal.json";
    private static final String STRONG_BACKGROUND_NOISE_RMS_INFO = "/audiofileinfo/signal/strong-background-noise-rms-info.json";
    private static final String STRONG_BACKGROUND_NOISE_AUDIO_CLIPS_INFO = "/audiofileinfo/signal/strong-background-noise-audio-clips-info.json";

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
        List<RmsSignalInfo> rmsSignalInfo = MAPPER.readValue(thisClass.getResourceAsStream(BACKGROUND_NOISE_LOW_VOLUME_RMS_INFO), new TypeReference<List<RmsSignalInfo>>() {});
        when(rmsCalculator.retrieveRmsInfo(anyObject(), anyInt(), anyInt())).thenReturn(rmsSignalInfo);

        AudioSignal backgroundNoiseAndLowVolumeSignal = MAPPER.readValue(thisClass.getResourceAsStream(BACKGROUND_NOISE_LOW_VOLUME_SIGNAL), AudioSignal.class);
        List<AudioClipInfo> actualAudioClipsInfo = soundZonesDetector.retrieveAudioClipsInfo(audioFileLocation.getAudioFileName(), backgroundNoiseAndLowVolumeSignal);
        List<AudioClipInfo> expectedAudioClipsInfo = MAPPER.readValue(thisClass.getResourceAsStream(BACKGROUND_NOISE_LOW_VOLUME_AUDIO_CLIPS_INFO), new TypeReference<List<AudioClipInfo>>() {});

        assertThat(actualAudioClipsInfo, is(expectedAudioClipsInfo));
    }

    @Test
    public void retrieveAudioClipsWithApplause() throws Exception {
        List<RmsSignalInfo> rmsSignalInfo = MAPPER.readValue(thisClass.getResourceAsStream(WITH_APPLAUSE_RMS_INFO), new TypeReference<List<RmsSignalInfo>>() {});
        when(rmsCalculator.retrieveRmsInfo(anyObject(), anyInt(), anyInt())).thenReturn(rmsSignalInfo);

        AudioSignal withApplauseSignal = MAPPER.readValue(thisClass.getResourceAsStream(WITH_APPLAUSE_SIGNAL), AudioSignal.class);
        List<AudioClipInfo> actualAudioClipsInfo = soundZonesDetector.retrieveAudioClipsInfo(audioFileLocation.getAudioFileName(), withApplauseSignal);
        List<AudioClipInfo> expectedAudioClipsInfo = MAPPER.readValue(thisClass.getResourceAsStream(WITH_APPLAUSE_AUDIO_CLIPS_INFO), new TypeReference<List<AudioClipInfo>>() {});

        assertThat(actualAudioClipsInfo, is(expectedAudioClipsInfo));
    }

    @Test
    public void retrieveAudioClipsWithStrongBackgroundNoise() throws Exception {
        List<RmsSignalInfo> rmsSignalInfo = MAPPER.readValue(thisClass.getResourceAsStream(STRONG_BACKGROUND_NOISE_RMS_INFO), new TypeReference<List<RmsSignalInfo>>() {});
        when(rmsCalculator.retrieveRmsInfo(anyObject(), anyInt(), anyInt())).thenReturn(rmsSignalInfo);

        AudioSignal withApplauseSignal = MAPPER.readValue(thisClass.getResourceAsStream(STRONG_BACKGROUND_NOISE_SIGNAL), AudioSignal.class);
        List<AudioClipInfo> actualAudioClipsInfo = soundZonesDetector.retrieveAudioClipsInfo(audioFileLocation.getAudioFileName(), withApplauseSignal);
        List<AudioClipInfo> expectedAudioClipsInfo = MAPPER.readValue(thisClass.getResourceAsStream(STRONG_BACKGROUND_NOISE_AUDIO_CLIPS_INFO), new TypeReference<List<AudioClipInfo>>() {});

        assertThat(actualAudioClipsInfo, is(expectedAudioClipsInfo));
    }

}
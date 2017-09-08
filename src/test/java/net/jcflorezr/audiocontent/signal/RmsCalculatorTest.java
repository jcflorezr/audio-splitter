package net.jcflorezr.audiocontent.signal;

import biz.source_code.dsp.model.AudioSignal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.model.audiocontent.signal.RmsSignalInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RmsCalculatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String BACKGROUND_NOISE_LOW_VOLUME_SIGNAL = "/audiocontent/signal/background-noise-low-volume-signal.json";
    private static final String BACKGROUND_NOISE_LOW_VOLUME_RMS_INFO = "/audiocontent/signal/background-noise-low-volume-rms-info.json";
    private static final String WITH_APPLAUSE_SIGNAL = "/audiocontent/signal/with-applause-signal.json";
    private static final String WITH_APPLAUSE_RMS_INFO = "/audiocontent/signal/with-applause-rms-info.json";
    private static final String STRONG_BACKGROUND_NOISE_SIGNAL = "/audiocontent/signal/strong-background-noise-signal.json";
    private static final String STRONG_BACKGROUND_NOISE_RMS_INFO = "/audiocontent/signal/strong-background-noise-rms-info.json";

    private Class<? extends RmsCalculatorTest> thisClass;

    @InjectMocks
    private RmsCalculator rmsCalculator;

    @Before
    public void setUp() throws Exception {
        thisClass = this.getClass();
    }

    @Test
    public void retrieveRmsInfoFromAudioWithBackgroundNoiseAndLowVolume() throws Exception {
        int samplingRate = 22050;
        int segmentSize = samplingRate / 10;
        float[][] signal = MAPPER.readValue(thisClass.getResourceAsStream(BACKGROUND_NOISE_LOW_VOLUME_SIGNAL), AudioSignal.class).getData();

        List<RmsSignalInfo> actualRmsSignalInfo = rmsCalculator.retrieveRmsInfo(signal, segmentSize, samplingRate);
        List<RmsSignalInfo> expectedRmsSignalInfo = MAPPER.readValue(thisClass.getResourceAsStream(BACKGROUND_NOISE_LOW_VOLUME_RMS_INFO), new TypeReference<List<RmsSignalInfo>>() {});

        assertThat(actualRmsSignalInfo, is(expectedRmsSignalInfo));
    }

    @Test
    public void retrieveRmsInfoFromAudioWithApplause() throws Exception {
        int samplingRate = 22050;
        int segmentSize = samplingRate / 10;
        float[][] signal = MAPPER.readValue(thisClass.getResourceAsStream(WITH_APPLAUSE_SIGNAL), AudioSignal.class).getData();

        List<RmsSignalInfo> actualRmsSignalInfo = rmsCalculator.retrieveRmsInfo(signal, segmentSize, samplingRate);
        List<RmsSignalInfo> expectedRmsSignalInfo = MAPPER.readValue(thisClass.getResourceAsStream(WITH_APPLAUSE_RMS_INFO), new TypeReference<List<RmsSignalInfo>>() {});

        assertThat(actualRmsSignalInfo, is(expectedRmsSignalInfo));
    }

    @Test
    public void retrieveRmsInfoFromAudioWithStrongBackgroundNoise() throws Exception {
        int samplingRate = 22050;
        int segmentSize = samplingRate / 10;
        float[][] signal = MAPPER.readValue(thisClass.getResourceAsStream(STRONG_BACKGROUND_NOISE_SIGNAL), AudioSignal.class).getData();

        List<RmsSignalInfo> actualRmsSignalInfo = rmsCalculator.retrieveRmsInfo(signal, segmentSize, samplingRate);
        List<RmsSignalInfo> expectedRmsSignalInfo = MAPPER.readValue(thisClass.getResourceAsStream(STRONG_BACKGROUND_NOISE_RMS_INFO), new TypeReference<List<RmsSignalInfo>>() {});

        assertThat(actualRmsSignalInfo, is(expectedRmsSignalInfo));
    }

}
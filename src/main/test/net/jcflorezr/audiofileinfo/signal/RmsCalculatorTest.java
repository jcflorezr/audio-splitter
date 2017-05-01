package net.jcflorezr.audiofileinfo.signal;

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
    private static final String BACKGROUND_NOISE_LOW_VOLUME_SIGNAL = "/audiofileinfo/signal/background-noise-low-volume-signal.json";
    private static final String BACKGROUND_NOISE_LOW_VOLUME_RESPONSE = "/audiofileinfo/signal/background-noise-low-volume-response.json";
    private static final String WITH_APPLAUSE_SIGNAL = "/audiofileinfo/signal/with-applause-signal.json";
    private static final String WITH_APPLAUSE_RESPONSE = "/audiofileinfo/signal/with-applause-response.json";
    private static final String STRONG_BACKGROUND_NOISE_SIGNAL = "/audiofileinfo/signal/strong-background-noise-signal.json";
    private static final String STRONG_BACKGROUND_NOISE_RESPONSE = "/audiofileinfo/signal/strong-background-noise-response.json";

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
        float[][] signal = MAPPER.readValue(thisClass.getResourceAsStream(BACKGROUND_NOISE_LOW_VOLUME_SIGNAL), float[][].class);

        List<RmsSignalInfo> actualRmsSignalInfo = rmsCalculator.retrieveRmsInfo(signal, segmentSize, samplingRate);
        List<RmsSignalInfo> expectedRmsSignalInfo = MAPPER.readValue(thisClass.getResourceAsStream(BACKGROUND_NOISE_LOW_VOLUME_RESPONSE), new TypeReference<List<RmsSignalInfo>>() {});

        assertThat(actualRmsSignalInfo, is(expectedRmsSignalInfo));
    }

    @Test
    public void retrieveRmsInfoFromAudioWithApplause() throws Exception {
        int samplingRate = 22050;
        int segmentSize = samplingRate / 10;
        float[][] signal = MAPPER.readValue(thisClass.getResourceAsStream(WITH_APPLAUSE_SIGNAL), float[][].class);

        List<RmsSignalInfo> actualRmsSignalInfo = rmsCalculator.retrieveRmsInfo(signal, segmentSize, samplingRate);
        List<RmsSignalInfo> expectedRmsSignalInfo = MAPPER.readValue(thisClass.getResourceAsStream(WITH_APPLAUSE_RESPONSE), new TypeReference<List<RmsSignalInfo>>() {});

        assertThat(actualRmsSignalInfo, is(expectedRmsSignalInfo));
    }

    @Test
    public void retrieveRmsInfoFromAudioWithStrongBackgroundNoise() throws Exception {
        int samplingRate = 22050;
        int segmentSize = samplingRate / 10;
        float[][] signal = MAPPER.readValue(thisClass.getResourceAsStream(STRONG_BACKGROUND_NOISE_SIGNAL), float[][].class);

        List<RmsSignalInfo> actualRmsSignalInfo = rmsCalculator.retrieveRmsInfo(signal, segmentSize, samplingRate);
        List<RmsSignalInfo> expectedRmsSignalInfo = MAPPER.readValue(thisClass.getResourceAsStream(STRONG_BACKGROUND_NOISE_RESPONSE), new TypeReference<List<RmsSignalInfo>>() {});

        assertThat(actualRmsSignalInfo, is(expectedRmsSignalInfo));
    }

}
package net.jcflorezr.audiofileinfo.signal;

import biz.source_code.dsp.model.AudioSignal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
public class SoundZonesDetectorImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String AUDIO_MONO_SIGNAL_JSON_FILE = "/audiofileinfo/signal/test-audio-mono-22050-signal.json";
    private static final String AUDIO_MONO_SOUND_ZONES_JSON_FILE = "/audiofileinfo/signal/audio-mono-22050-sound-zones.json";

    private Class<? extends SoundZonesDetectorImplTest> thisClass;

    @InjectMocks
    private SoundZonesDetectorImpl soundZonesDetector;

    @Before
    public void setUp() throws Exception {
        thisClass = this.getClass();
    }

    @Test
    public void retrieveAudioSoundZonesFromAudioMonoFileWithSamplingRateOf22050() throws Exception {
        JsonNode audioSignalJson = MAPPER.readTree(thisClass.getResourceAsStream(AUDIO_MONO_SIGNAL_JSON_FILE));
        AudioSignal audioSignal = MAPPER.convertValue(audioSignalJson, AudioSignal.class);

        String actualAudioSoundZones = MAPPER.convertValue(soundZonesDetector.getAudioSoundZones(audioSignal), JsonNode.class).toString();
        String expectedAudioSoundZones = MAPPER.readTree(thisClass.getResourceAsStream(AUDIO_MONO_SOUND_ZONES_JSON_FILE)).toString();

        assertEquals(expectedAudioSoundZones, actualAudioSoundZones);
    }

    @Test
    public void retrieveAudioSoundZonesFromAudioMonoFileWithSamplingRateOf44100() throws Exception {
        JsonNode audioSignalJson = MAPPER.readTree(thisClass.getResourceAsStream(AUDIO_MONO_SIGNAL_JSON_FILE));
        AudioSignal audioSignal = MAPPER.convertValue(audioSignalJson, AudioSignal.class);

        String actualAudioSoundZones = MAPPER.convertValue(soundZonesDetector.getAudioSoundZones(audioSignal), JsonNode.class).toString();
        String expectedAudioSoundZones = MAPPER.readTree(thisClass.getResourceAsStream(AUDIO_MONO_SOUND_ZONES_JSON_FILE)).toString();

        assertEquals(expectedAudioSoundZones, actualAudioSoundZones);
    }

}
package net.jcflorezr.audiofileinfo;

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class AudioContentServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String EMPTY_AUDIO_METADATA_JSON_FILE = "/audiocontent/emptyAudioMetadata.json";
    private static final String MP3_AUDIO_METADATA_JSON_FILE = "/audiocontent/mp3AudioMetadata.json";

    private String testResourcesPath;
    private Class<? extends AudioContentServiceTest> thisClass;

    @Mock
    private AudioIo audioIo;
    @InjectMocks
    private AudioContentService audioContentService;

    @Before
    public void setUp() {
        thisClass = this.getClass();
        testResourcesPath = thisClass.getResource("/").getPath();
    }

    @Test
    public void retrieveAudioContent() throws Exception {
        // Given
        String audioFileName = testResourcesPath + "test-audio-mono-22050.mp3";
        String convertedAudioFileName = "/any-path-to-file/any-file.mp3";
        AudioFileInfo dummyAudioFileInfo = createDummyAudioFileInfo(audioFileName, convertedAudioFileName);
        AudioSignal dummyAudioSignal = createDummyAudioSignal(22050, new float[][]{});

        // When
        when(audioIo.loadWavFile(anyString())).thenReturn(dummyAudioSignal);
        AudioContent actualAudioContent = audioContentService.retrieveAudioContent(dummyAudioFileInfo);

        // Then
        float[][] emptyAudioSignalData = new float[][]{};
        assertEquals(emptyAudioSignalData, actualAudioContent.getOriginalAudioData());

        JsonNode emptyAudioMetadata = MAPPER.readTree(thisClass.getResourceAsStream(MP3_AUDIO_METADATA_JSON_FILE));
        JsonNode actualAudioMetadata = MAPPER.convertValue(actualAudioContent.getAudioMetadata(), JsonNode.class);
        assertEquals(emptyAudioMetadata, actualAudioMetadata);
    }

    @Test
    public void retrieveEmptyAudioContent() throws Exception {
        // Given
        String audioFileName = testResourcesPath + "test-audio-mono-22050.wav";
        String convertedAudioFileName = "/any-path-to-file/any-file.wav";
        AudioFileInfo dummyAudioFileInfo = createDummyAudioFileInfo(audioFileName, convertedAudioFileName);
        AudioSignal dummyAudioSignal = createDummyAudioSignal(22050, new float[][]{});

        // When
        when(audioIo.loadWavFile(anyString())).thenReturn(dummyAudioSignal);
        AudioContent actualAudioContent = audioContentService.retrieveAudioContent(dummyAudioFileInfo);

        // Then
        float[][] emptyAudioSignalData = new float[][]{};
        assertEquals(emptyAudioSignalData, actualAudioContent.getOriginalAudioData());

        JsonNode emptyAudioMetadata = MAPPER.readTree(thisClass.getResourceAsStream(EMPTY_AUDIO_METADATA_JSON_FILE));
        JsonNode actualAudioMetadata = MAPPER.convertValue(actualAudioContent.getAudioMetadata(), JsonNode.class);
        assertEquals(emptyAudioMetadata, actualAudioMetadata);
    }

    private AudioFileInfo createDummyAudioFileInfo(String audioFileName, String convertedAudioFileName) {
        AudioFileLocation audioFileLocation = new AudioFileLocation(audioFileName, null);
        AudioFileInfo audioFileInfo = new AudioFileInfo(audioFileLocation);
        audioFileInfo.setConvertedAudioFileName(convertedAudioFileName);
        return audioFileInfo;
    }

    private AudioSignal createDummyAudioSignal(int samplingRate, float[][] signalData) {
        return new AudioSignal(samplingRate, signalData);
    }

}
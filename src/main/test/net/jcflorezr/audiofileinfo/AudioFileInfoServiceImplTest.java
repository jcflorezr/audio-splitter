package net.jcflorezr.audiofileinfo;

import biz.source_code.dsp.model.AudioSignal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.audiofileinfo.signal.SoundZonesDetectorImpl;
import net.jcflorezr.model.audioclips.AudioClipInfo;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.audiocontent.AudioMetadata;
import net.jcflorezr.model.request.AudioFileLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class AudioFileInfoServiceImplTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String MP3_AUDIO_METADATA_JSON_FILE = "/audiofileinfo/mp3AudioMetadata.json";
    private static final String AUDIO_CLIPS_INFO_JSON_FILE = "/audiofileinfo/audioClipsInfo.json";
    private static final String AUDIO_FILE_INFO_JSON_FILE = "/audiofileinfo/audioFileInfo.json";

    private Class<? extends AudioFileInfoServiceImplTest> thisClass;

    @Mock
    private AudioConverterService audioConverterService;
    @Mock
    private AudioContentService audioContentService;
    @Mock
    private SoundZonesDetectorImpl soundZonesDetector;
    @InjectMocks
    private AudioFileInfoServiceImpl audioFileInfoService;

    @Before
    public void setUp() {
        thisClass = this.getClass();
    }

    @Test
    public void generateAudioFileInfo() throws Exception {

        // Given
        String convertedAudioFileName = "any-converted-audio-file-name";
        AudioContent audioContent = createDummyAudioContent();
        List<AudioClipInfo> audioClipsInfo = createDummyAudioClipsInfo();

        // When
        when(audioConverterService.convertFileToWavIfNeeded(anyString())).thenReturn(convertedAudioFileName);
        when(audioContentService.retrieveAudioContent(anyObject())).thenReturn(audioContent);
        when(soundZonesDetector.getAudioSoundZones(anyObject())).thenReturn(audioClipsInfo);

        AudioFileLocation audioFileLocation = createDummyAudioFileLocation();
        boolean grouped = false;
        AudioFileInfo actualAudioFileInfo = audioFileInfoService.generateAudioFileInfo(audioFileLocation, grouped);

        // Then
        String expectedAudioFileInfo = MAPPER.readTree(thisClass.getResourceAsStream(AUDIO_FILE_INFO_JSON_FILE)).toString();
        String actualAudioFileInfoJson = MAPPER.convertValue(actualAudioFileInfo, JsonNode.class).toString();

        assertEquals(expectedAudioFileInfo, actualAudioFileInfoJson);
    }

    private AudioFileLocation createDummyAudioFileLocation() {
        String audioFileName = "any-audio-file-name";
        String outputDirectoryPath = "any-output-directory-path";
        return new AudioFileLocation(audioFileName, outputDirectoryPath);
    }

    private AudioContent createDummyAudioContent() throws IOException {
        AudioSignal dummyAudioSignal = new AudioSignal(0, new float[][]{});
        JsonNode dummyAudioMetadataJson = MAPPER.readTree(thisClass.getResourceAsStream(MP3_AUDIO_METADATA_JSON_FILE));
        AudioMetadata dummyAudioMetadata = MAPPER.convertValue(dummyAudioMetadataJson, AudioMetadata.class);
        return new AudioContent(dummyAudioSignal, dummyAudioMetadata);
    }

    private List<AudioClipInfo> createDummyAudioClipsInfo() throws IOException {
        JsonNode dummyAudioClipsInfoJson = MAPPER.readTree(thisClass.getResourceAsStream(AUDIO_CLIPS_INFO_JSON_FILE));
        return MAPPER.convertValue(dummyAudioClipsInfoJson,
                    MAPPER.getTypeFactory().constructCollectionType(List.class, AudioClipInfo.class));
    }

}
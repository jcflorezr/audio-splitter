package net.jcflorezr.audiofileinfo;

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.audiocontent.AudioMetadata;
import net.jcflorezr.model.request.AudioFileLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AudioContentServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String EMPTY_AUDIO_METADATA_JSON_FILE = "/audiofileinfo/emptyAudioMetadata.json";
    private static final String MP3_AUDIO_METADATA_JSON_FILE = "/audiofileinfo/mp3AudioMetadata.json";

    private String testResourcesPath;
    private Class<? extends AudioContentServiceTest> thisClass;

    @Mock
    private AudioIo audioIo;
    @InjectMocks
    private AudioContentService audioContentService;

    @Before
    public void setUp() {
        thisClass = this.getClass();
        testResourcesPath = thisClass.getResource("/audiofileinfo/").getPath();
    }

    @Test
    public void retrieveAudioContent() throws Exception {
        String audioFileName = testResourcesPath + "test-audio-mono-22050.mp3";
        String convertedAudioFileName = "/any-path-to-file/any-file.mp3";
        AudioFileInfo dummyAudioFileInfo = createDummyAudioFileInfo(audioFileName, convertedAudioFileName);
        AudioSignal dummyAudioSignal = createDummyAudioSignal(22050, new float[][]{});

        when(audioIo.retrieveAudioSignalFromWavFile(anyString())).thenReturn(dummyAudioSignal);

        AudioContent actualAudioContent = audioContentService.retrieveAudioContent(dummyAudioFileInfo);

        float[][] emptyAudioSignalData = new float[][]{};
        float[][] actualAudioSignalData = actualAudioContent.getOriginalAudioData();
        assertThat(actualAudioSignalData, is(emptyAudioSignalData));

        AudioMetadata mp3AudioMetadata = MAPPER.readValue(thisClass.getResourceAsStream(MP3_AUDIO_METADATA_JSON_FILE), AudioMetadata.class);
        AudioMetadata actualAudioMetadata = actualAudioContent.getAudioMetadata();
        assertThat(actualAudioMetadata, is(mp3AudioMetadata));
    }

    @Test
    public void audioMetadataIsNotRetrievedForWavFiles() throws Exception {
        String audioFileName = testResourcesPath + "test-audio-mono-22050.wav";
        String convertedAudioFileName = "/any-path-to-file/any-file.wav";
        AudioFileInfo dummyAudioFileInfo = createDummyAudioFileInfo(audioFileName, convertedAudioFileName);
        AudioSignal dummyAudioSignal = createDummyAudioSignal(22050, new float[][]{});

        when(audioIo.retrieveAudioSignalFromWavFile(anyString())).thenReturn(dummyAudioSignal);

        AudioContent actualAudioContent = audioContentService.retrieveAudioContent(dummyAudioFileInfo);

        float[][] emptyAudioSignalData = new float[][]{};
        float[][] actualAudioSignalData = actualAudioContent.getOriginalAudioData();
        assertThat(actualAudioSignalData, is(emptyAudioSignalData));

        AudioMetadata emptyAudioMetadata = MAPPER.readValue(thisClass.getResourceAsStream(EMPTY_AUDIO_METADATA_JSON_FILE), AudioMetadata.class);
        AudioMetadata actualAudioMetadata = actualAudioContent.getAudioMetadata();
        assertThat(actualAudioMetadata, is(emptyAudioMetadata));
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
package net.jcflorezr.audiocontent;

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.audiocontent.AudioFileMetadataEntity;
import net.jcflorezr.model.endpoint.AudioFileBasicInfoEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AudioContentServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String EMPTY_AUDIO_METADATA_JSON_FILE = "/audiofileinfo/empty-audio-metadata.json";
    private static final String MP3_AUDIO_METADATA_JSON_FILE = "/audiofileinfo/mp3-audio-metadata.json";

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
        AudioFileCompleteInfo dummyAudioFileCompleteInfo = createDummyAudioFileInfo(new File(audioFileName).getPath(), convertedAudioFileName);
        AudioSignal dummyAudioSignal = createDummyAudioSignal(22050, new float[][]{});

        when(audioIo.retrieveAudioSignalFromWavFile(anyString())).thenReturn(dummyAudioSignal);

        AudioContent actualAudioContent = audioContentService.retrieveAudioContent(dummyAudioFileCompleteInfo);

        float[][] emptyAudioSignalData = new float[][]{};
        float[][] actualAudioSignalData = actualAudioContent.getOriginalAudioData();
        assertThat(actualAudioSignalData, equalTo(emptyAudioSignalData));

        AudioFileMetadataEntity mp3AudioFileMetadataEntity = MAPPER.readValue(thisClass.getResourceAsStream(MP3_AUDIO_METADATA_JSON_FILE), AudioFileMetadataEntity.class);
        AudioFileMetadataEntity actualAudioFileMetadataEntity = actualAudioContent.getAudioFileMetadataEntity();

        assertThat(actualAudioFileMetadataEntity.getTitle(), equalTo(mp3AudioFileMetadataEntity.getTitle()));
        assertThat(actualAudioFileMetadataEntity.getArtist(), equalTo(mp3AudioFileMetadataEntity.getArtist()));
        assertThat(actualAudioFileMetadataEntity.getAlbum(), equalTo(mp3AudioFileMetadataEntity.getAlbum()));
        assertThat(actualAudioFileMetadataEntity.getTrackNumber(), equalTo(mp3AudioFileMetadataEntity.getTrackNumber()));
        assertThat(actualAudioFileMetadataEntity.getGenre(), equalTo(mp3AudioFileMetadataEntity.getGenre()));
        assertThat(actualAudioFileMetadataEntity.getComments(), equalTo(mp3AudioFileMetadataEntity.getComments()));
        assertThat(actualAudioFileMetadataEntity.getRawMetadata(), equalTo(mp3AudioFileMetadataEntity.getRawMetadata()));
    }

    @Test
    public void audioMetadataIsNotRetrievedForWavFiles() throws Exception {
        String audioFileName = testResourcesPath + "test-audio-mono-22050.wav";
        String convertedAudioFileName = "/any-path-to-file/any-file.wav";
        AudioFileCompleteInfo dummyAudioFileCompleteInfo = createDummyAudioFileInfo(audioFileName, convertedAudioFileName);
        AudioSignal dummyAudioSignal = createDummyAudioSignal(22050, new float[][]{});

        when(audioIo.retrieveAudioSignalFromWavFile(anyString())).thenReturn(dummyAudioSignal);

        AudioContent actualAudioContent = audioContentService.retrieveAudioContent(dummyAudioFileCompleteInfo);

        float[][] emptyAudioSignalData = new float[][]{};
        float[][] actualAudioSignalData = actualAudioContent.getOriginalAudioData();
        assertThat(actualAudioSignalData, is(emptyAudioSignalData));

        AudioFileMetadataEntity actualAudioFileMetadataEntity = actualAudioContent.getAudioFileMetadataEntity();

        assertNull(actualAudioFileMetadataEntity.getTitle());
        assertNull(actualAudioFileMetadataEntity.getArtist());
        assertNull(actualAudioFileMetadataEntity.getAlbum());
        assertNull(actualAudioFileMetadataEntity.getTrackNumber());
        assertNull(actualAudioFileMetadataEntity.getGenre());
        assertNull(actualAudioFileMetadataEntity.getComments());
        assertTrue(actualAudioFileMetadataEntity.getRawMetadata().isEmpty());
    }

    private AudioFileCompleteInfo createDummyAudioFileInfo(String audioFileName, String convertedAudioFileName) {
        AudioFileBasicInfoEntity audioFileBasicInfoEntity = new AudioFileBasicInfoEntity(audioFileName, null);
        audioFileBasicInfoEntity.setConvertedAudioFileName(convertedAudioFileName);
        AudioFileCompleteInfo audioFileCompleteInfo = new AudioFileCompleteInfo(audioFileBasicInfoEntity);
        return audioFileCompleteInfo;
    }

    private AudioSignal createDummyAudioSignal(int samplingRate, float[][] signalData) {
        return new AudioSignal(samplingRate, signalData);
    }

}
package net.jcflorezr.audiofileinfo;

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.audiocontent.AudioFileMetadata;
import net.jcflorezr.model.request.AudioFileBasicInfo;
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

        AudioFileMetadata mp3AudioFileMetadata = MAPPER.readValue(thisClass.getResourceAsStream(MP3_AUDIO_METADATA_JSON_FILE), AudioFileMetadata.class);
        AudioFileMetadata actualAudioFileMetadata = actualAudioContent.getAudioFileMetadata();

        assertThat(actualAudioFileMetadata.getTitle(), equalTo(mp3AudioFileMetadata.getTitle()));
        assertThat(actualAudioFileMetadata.getArtist(), equalTo(mp3AudioFileMetadata.getArtist()));
        assertThat(actualAudioFileMetadata.getAlbum(), equalTo(mp3AudioFileMetadata.getAlbum()));
        assertThat(actualAudioFileMetadata.getTrackNumber(), equalTo(mp3AudioFileMetadata.getTrackNumber()));
        assertThat(actualAudioFileMetadata.getGenre(), equalTo(mp3AudioFileMetadata.getGenre()));
        assertThat(actualAudioFileMetadata.getComments(), equalTo(mp3AudioFileMetadata.getComments()));
        assertThat(actualAudioFileMetadata.getRawMetadata(), equalTo(mp3AudioFileMetadata.getRawMetadata()));
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

        AudioFileMetadata actualAudioFileMetadata = actualAudioContent.getAudioFileMetadata();

        assertNull(actualAudioFileMetadata.getTitle());
        assertNull(actualAudioFileMetadata.getArtist());
        assertNull(actualAudioFileMetadata.getAlbum());
        assertNull(actualAudioFileMetadata.getTrackNumber());
        assertNull(actualAudioFileMetadata.getGenre());
        assertNull(actualAudioFileMetadata.getComments());
        assertTrue(actualAudioFileMetadata.getRawMetadata().isEmpty());
    }

    private AudioFileCompleteInfo createDummyAudioFileInfo(String audioFileName, String convertedAudioFileName) {
        AudioFileBasicInfo audioFileBasicInfo = new AudioFileBasicInfo(audioFileName, null);
        audioFileBasicInfo.setConvertedAudioFileName(convertedAudioFileName);
        AudioFileCompleteInfo audioFileCompleteInfo = new AudioFileCompleteInfo(audioFileBasicInfo);
        return audioFileCompleteInfo;
    }

    private AudioSignal createDummyAudioSignal(int samplingRate, float[][] signalData) {
        return new AudioSignal(samplingRate, signalData);
    }

}
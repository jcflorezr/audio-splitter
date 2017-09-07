package net.jcflorezr.api.endpoint;

import biz.source_code.dsp.util.AudioFormatsSupported;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.api.audiocontent.AudioFileInfoService;
import net.jcflorezr.api.persistence.PersistenceService;
import net.jcflorezr.endpoint.FlacAudioSplitterBySingleFiles;
import net.jcflorezr.exceptions.BadRequestException;
import net.jcflorezr.model.audioclips.AudioFileClipResultEntity;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.endpoint.AudioFileBasicInfoEntity;
import net.jcflorezr.model.endpoint.AudioSplitterResponse;
import net.jcflorezr.model.endpoint.SuccessResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AudioSplitterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String AUDIO_CLIPS_WRITING_RESULT = "/api/endpoint/audio-clips-writing-result.json";

    private String testResourcesPath;
    private Class<? extends AudioSplitterTest> thisClass;
    private final AudioFormatsSupported audioFormat = AudioFormatsSupported.FLAC;
    private final boolean asMono = false;

    @Mock
    private AudioFileInfoService audioFileInfoService;
    @Mock
    private AudioClipsGenerator audioClipsGenerator;
    @Mock
    private PersistenceService persistenceService;

    @InjectMocks
    private FlacAudioSplitterBySingleFiles audioSplitter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        thisClass = this.getClass();
        testResourcesPath = thisClass.getResource("/api/endpoint/").toURI().getPath();
    }

    @Test
    public void generateAudioClips() throws Exception {
        String audioFileName = testResourcesPath + "test-input-directory/test-audio-file.mp3";
        String outputAudioClipsDirectoryPath = testResourcesPath + "test-output-directory/";
        AudioFileBasicInfoEntity dummyAudioFileBasicInfoEntity = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        when(audioFileInfoService.generateAudioFileInfo(anyObject(), anyBoolean())).thenReturn(new AudioFileCompleteInfo(dummyAudioFileBasicInfoEntity));
        List<AudioFileClipResultEntity> mockAudioFileClipResultEntity = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_CLIPS_WRITING_RESULT), new TypeReference<List<AudioFileClipResultEntity>>(){});
        when(audioClipsGenerator.generateAudioClip(anyString(), anyObject(), anyObject(), anyBoolean())).thenReturn(mockAudioFileClipResultEntity);
        doNothing().when(persistenceService).storeResults(anyObject(), anyObject());

        AudioSplitterResponse actualAudioSplitterResponse = audioSplitter.generateAudioClips(dummyAudioFileBasicInfoEntity, audioFormat, asMono);
        assertTrue(actualAudioSplitterResponse instanceof SuccessResponse);

        long actualNumOfSuccessAudioClips = ((SuccessResponse) actualAudioSplitterResponse).getNumOfSuccessAudioClips();
        long expectedNumOfSuccessAudioClips = 10;
        assertThat(actualNumOfSuccessAudioClips, is(expectedNumOfSuccessAudioClips));

        long actualNumOfFailedAudioClips = ((SuccessResponse) actualAudioSplitterResponse).getNumOfFailedAudioClips();
        long expectedNumOfFailedAudioClips = 5;
        assertThat(actualNumOfFailedAudioClips, is(expectedNumOfFailedAudioClips));
    }

    @Test
    public void shouldThrow_AudioFileDoesNotExist_ErrorMessage() throws Exception {
        String audioFileName = "any-audio-file";
        String outputAudioClipsDirectoryPath = "any-output-directory";
        AudioFileBasicInfoEntity dummyAudioFileBasicInfoEntity = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("The audio file 'any-audio-file' does not exist.");

        audioSplitter.generateAudioClips(dummyAudioFileBasicInfoEntity, audioFormat, asMono);
    }

    @Test
    public void shouldThrow_AudioFileShouldNotBeDirectory_ErrorMessage() throws Exception {
        String audioFileName = "test-input-directory";
        String outputAudioClipsDirectoryPath = "any-output-directory";
        AudioFileBasicInfoEntity dummyAudioFileBasicInfoEntity = createDummyAudioFileLocation(testResourcesPath + audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        String expectedErrorMessage = "'" + new File(testResourcesPath + audioFileName).getPath() + "' should be a file, not a directory.";
        expectedException.expectMessage(expectedErrorMessage);

        audioSplitter.generateAudioClips(dummyAudioFileBasicInfoEntity, audioFormat, asMono);
    }

    @Test
    public void shouldThrow_OutputDirectoryDoesNotExist_ErrorMessage() throws Exception {
        String audioFileName = testResourcesPath + "test-input-directory/test-audio-file.mp3";
        String outputAudioClipsDirectoryPath = "any-output-directory";
        AudioFileBasicInfoEntity dummyAudioFileBasicInfoEntity = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("The directory 'any-output-directory' does not exist.");

        audioSplitter.generateAudioClips(dummyAudioFileBasicInfoEntity, audioFormat, asMono);
    }

    @Test
    public void shouldThrow_SameAudioFileAndOutputDirectoryLocation_ErrorMessage() throws Exception {
        String audioFileName = testResourcesPath + "test-input-directory/test-audio-file.mp3";
        String outputAudioClipsDirectoryPath = testResourcesPath + "test-input-directory";
        AudioFileBasicInfoEntity dummyAudioFileBasicInfoEntity = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("The audio file location cannot be the same as the output audio clips location.");

        audioSplitter.generateAudioClips(dummyAudioFileBasicInfoEntity, audioFormat, asMono);
    }

    @Test
    public void shouldThrow_EmptyAudioFileLocationObject_ErrorMessage() throws Exception {
        AudioFileBasicInfoEntity dummyAudioFileBasicInfoEntity = null;

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("There is no body in the current endpoint.");

        audioSplitter.generateAudioClips(dummyAudioFileBasicInfoEntity, audioFormat, asMono);
    }

    @Test
    public void shouldThrow_EmptyMandatoryFields_ErrorMessage() throws Exception {
        // With empty audioFileName
        String audioFileName = "";
        String outputAudioClipsDirectoryPath = testResourcesPath + "test-input-directory";
        AudioFileBasicInfoEntity dummyAudioFileBasicInfoEntity = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("There are empty mandatory fields.");

        audioSplitter.generateAudioClips(dummyAudioFileBasicInfoEntity, audioFormat, asMono);

        // With empty outputAudioClipsDirectoryPath
        audioFileName = testResourcesPath + "test-input-directory/test-audio-file.mp3";
        outputAudioClipsDirectoryPath = "";
        dummyAudioFileBasicInfoEntity = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("There are empty mandatory fields.");

        audioSplitter.generateAudioClips(dummyAudioFileBasicInfoEntity, audioFormat, asMono);
    }

    private AudioFileBasicInfoEntity createDummyAudioFileLocation (String audioFileName, String outputAudioClipsDirectoryPath) {
        return new AudioFileBasicInfoEntity(audioFileName, outputAudioClipsDirectoryPath);
    }

}
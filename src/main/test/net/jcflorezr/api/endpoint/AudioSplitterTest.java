package net.jcflorezr.api.endpoint;

import biz.source_code.dsp.util.AudioFormatsSupported;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.api.audiofileinfo.AudioFileInfoService;
import net.jcflorezr.endpoint.FlacAudioSplitterBySingleFiles;
import net.jcflorezr.exceptions.BadRequestException;
import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;
import net.jcflorezr.model.response.AudioSplitterResponse;
import net.jcflorezr.model.response.SuccessResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
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

    @InjectMocks
    private FlacAudioSplitterBySingleFiles audioSplitter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        thisClass = this.getClass();
        testResourcesPath = thisClass.getResource("/api/endpoint/").getPath();
    }

    @Test
    public void generateAudioClips() throws Exception {
        String audioFileName = testResourcesPath + "test-input-directory/test-audio-file.mp3";
        String outputAudioClipsDirectoryPath = testResourcesPath + "test-output-directory/";
        AudioFileLocation dummyAudioFileLocation = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        when(audioFileInfoService.generateAudioFileInfo(anyObject(), anyBoolean())).thenReturn(new AudioFileInfo(dummyAudioFileLocation));
        List<AudioClipsWritingResult> mockAudioClipsWritingResult = MAPPER.readValue(thisClass.getResourceAsStream(AUDIO_CLIPS_WRITING_RESULT), new TypeReference<List<AudioClipsWritingResult>>(){});
        when(audioClipsGenerator.generateAudioClip(audioFileLocation.getAudioFileName(), anyObject(), anyObject(), anyBoolean())).thenReturn(mockAudioClipsWritingResult);

        AudioSplitterResponse actualAudioSplitterResponse = audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
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
        AudioFileLocation dummyAudioFileLocation = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("The audio file 'any-audio-file' does not exist.");

        audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
    }

    @Test
    public void shouldThrow_AudioFileShouldNotBeDirectory_ErrorMessage() throws Exception {
        String audioFileName = "test-input-directory";
        String outputAudioClipsDirectoryPath = "any-output-directory";
        AudioFileLocation dummyAudioFileLocation = createDummyAudioFileLocation(testResourcesPath + audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        String expectedErrorMessage = "'" + testResourcesPath + audioFileName + "' should be a file, not a directory.";
        expectedException.expectMessage(expectedErrorMessage);

        audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
    }

    @Test
    public void shouldThrow_OutputDirectoryDoesNotExist_ErrorMessage() throws Exception {
        String audioFileName = testResourcesPath + "test-input-directory/test-audio-file.mp3";
        String outputAudioClipsDirectoryPath = "any-output-directory";
        AudioFileLocation dummyAudioFileLocation = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("The directory 'any-output-directory' does not exist.");

        audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
    }

    @Test
    public void shouldThrow_SameAudioFileAndOutputDirectoryLocation_ErrorMessage() throws Exception {
        String audioFileName = testResourcesPath + "test-input-directory/test-audio-file.mp3";
        String outputAudioClipsDirectoryPath = testResourcesPath + "test-input-directory";
        AudioFileLocation dummyAudioFileLocation = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("The audio file location cannot be the same as the output audio clips location.");

        audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
    }

    @Test
    public void shouldThrow_EmptyAudioFileLocationObject_ErrorMessage() throws Exception {
        AudioFileLocation dummyAudioFileLocation = null;

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("There is no body in the current request.");

        audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
    }

    @Test
    public void shouldThrow_EmptyMandatoryFields_ErrorMessage() throws Exception {
        // With empty audioFileName
        String audioFileName = "";
        String outputAudioClipsDirectoryPath = testResourcesPath + "test-input-directory";
        AudioFileLocation dummyAudioFileLocation = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("There are empty mandatory fields.");

        audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);

        // With empty outputAudioClipsDirectoryPath
        audioFileName = testResourcesPath + "test-input-directory/test-audio-file.mp3";
        outputAudioClipsDirectoryPath = "";
        dummyAudioFileLocation = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        expectedException.expect(BadRequestException.class);
        expectedException.expectMessage("There are empty mandatory fields.");

        audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
    }

    private AudioFileLocation createDummyAudioFileLocation (String audioFileName, String outputAudioClipsDirectoryPath) {
        return new AudioFileLocation(audioFileName, outputAudioClipsDirectoryPath, null);
    }

}
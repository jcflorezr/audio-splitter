package net.jcflorezr.api.endpoint;

import biz.source_code.dsp.util.AudioFormatsSupported;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jcflorezr.api.audioclips.AudioClipsGenerator;
import net.jcflorezr.api.audiofileinfo.AudioFileInfoService;
import net.jcflorezr.endpoint.FlacAudioSplitterBySingleFiles;
import net.jcflorezr.exceptions.AudioFileLocationException;
import net.jcflorezr.exceptions.AudioSplitterCustomException;
import net.jcflorezr.model.audioclips.AudioClipsWritingResult;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;
import net.jcflorezr.model.response.AudioSplitterResponse;
import net.jcflorezr.model.response.ErrorResponse;
import net.jcflorezr.model.response.SuccessResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static junit.framework.TestCase.assertNull;
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
        when(audioClipsGenerator.generateAudioClip(anyObject(), anyObject(), anyBoolean())).thenReturn(mockAudioClipsWritingResult);

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

        AudioSplitterResponse actualAudioSplitterResponse = audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
        assertTrue(actualAudioSplitterResponse instanceof ErrorResponse);

        AudioSplitterCustomException audioSplitterCustomException = ((ErrorResponse) actualAudioSplitterResponse).getAudioSplitterCustomException();
        assertTrue(audioSplitterCustomException instanceof AudioFileLocationException);

        String actualErrorMessage = audioSplitterCustomException.getMessage();
        String expectedErrorMessage = "The audio file 'any-audio-file' does not exist.";
        assertThat(actualErrorMessage, is(expectedErrorMessage));

        String actualErrorSuggestion = ((AudioFileLocationException) audioSplitterCustomException).getSuggestion();
        assertNull(actualErrorSuggestion);
    }

    @Test
    public void shouldThrow_AudioFileShouldNotBeDirectory_ErrorMessage() throws Exception {
        String audioFileName = "test-input-directory";
        String outputAudioClipsDirectoryPath = "any-output-directory";
        AudioFileLocation dummyAudioFileLocation = createDummyAudioFileLocation(testResourcesPath + audioFileName, outputAudioClipsDirectoryPath);

        AudioSplitterResponse actualAudioSplitterResponse = audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
        assertTrue(actualAudioSplitterResponse instanceof ErrorResponse);

        AudioSplitterCustomException audioSplitterCustomException = ((ErrorResponse) actualAudioSplitterResponse).getAudioSplitterCustomException();
        assertTrue(audioSplitterCustomException instanceof AudioFileLocationException);

        String actualErrorMessage = audioSplitterCustomException.getMessage();
        String expectedErrorMessage = "'" + testResourcesPath + audioFileName + "' should be a file, not a directory.";
        assertThat(actualErrorMessage, is(expectedErrorMessage));

        String actualErrorSuggestion = ((AudioFileLocationException) audioSplitterCustomException).getSuggestion();
        assertNull(actualErrorSuggestion);
    }

    @Test
    public void shouldThrow_OutputDirectoryDoesNotExist_ErrorMessage() throws Exception {
        String audioFileName = testResourcesPath + "test-input-directory/test-audio-file.mp3";
        String outputAudioClipsDirectoryPath = "any-output-directory";
        AudioFileLocation dummyAudioFileLocation = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        AudioSplitterResponse actualAudioSplitterResponse = audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
        assertTrue(actualAudioSplitterResponse instanceof ErrorResponse);

        AudioSplitterCustomException audioSplitterCustomException = ((ErrorResponse) actualAudioSplitterResponse).getAudioSplitterCustomException();
        assertTrue(audioSplitterCustomException instanceof AudioFileLocationException);

        String actualErrorMessage = audioSplitterCustomException.getMessage();
        String expectedErrorMessage = "The directory 'any-output-directory' does not exist.";
        assertThat(actualErrorMessage, is(expectedErrorMessage));

        String actualErrorSuggestion = ((AudioFileLocationException) audioSplitterCustomException).getSuggestion();
        assertNull(actualErrorSuggestion);
    }

    @Test
    public void shouldThrow_SameAudioFileAndOutputDirectoryLocation_ErrorMessage() throws Exception {
        String audioFileName = testResourcesPath + "test-input-directory/test-audio-file.mp3";
        String outputAudioClipsDirectoryPath = testResourcesPath + "test-input-directory";
        AudioFileLocation dummyAudioFileLocation = createDummyAudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);

        AudioSplitterResponse actualAudioSplitterResponse = audioSplitter.generateAudioClips(dummyAudioFileLocation, audioFormat, asMono);
        assertTrue(actualAudioSplitterResponse instanceof ErrorResponse);

        AudioSplitterCustomException audioSplitterCustomException = ((ErrorResponse) actualAudioSplitterResponse).getAudioSplitterCustomException();
        assertTrue(audioSplitterCustomException instanceof AudioFileLocationException);

        String actualErrorMessage = audioSplitterCustomException.getMessage();
        String expectedErrorMessage = "The audio file location cannot be the same as the output audio clips location.";
        assertThat(actualErrorMessage, is(expectedErrorMessage));

        String actualErrorSuggestion = ((AudioFileLocationException) audioSplitterCustomException).getSuggestion();
        assertNull(actualErrorSuggestion);
    }

    private AudioFileLocation createDummyAudioFileLocation (String audioFileName, String outputAudioClipsDirectoryPath) {
        return new AudioFileLocation(audioFileName, outputAudioClipsDirectoryPath);
    }

}
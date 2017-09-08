package net.jcflorezr.audiocontent;

import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.util.AudioUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.sound.sampled.UnsupportedAudioFileException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AudioUtils.class})
public class AudioConverterServiceTest {

    @InjectMocks
    private AudioConverterService audioConverterService;

    @Before
    public void setUp() {
        mockStatic(AudioUtils.class);
    }

    @Test
    public void shouldConvertFileToWav() throws UnsupportedAudioFileException {
        when(AudioUtils.convertAudioFile(anyString(), anyString())).thenReturn(true);
        String audioFileName = "/any-path-to-file/any-file.mp3";
        String expectedConvertedAudioFileName = "/any-path-to-file/any-file.wav";
        String actualConvertedAudioFileName = audioConverterService.convertFileToWavIfNeeded(audioFileName);
        assertThat(actualConvertedAudioFileName, is(expectedConvertedAudioFileName));
    }

    @Test
    public void audioFileFormatIsAlreadyWav() throws UnsupportedAudioFileException {
        when(AudioUtils.convertAudioFile(anyString(), anyString())).thenReturn(true);
        String audioFileName = "/any-path-to-file/any-file.wav";
        String expectedConvertedAudioFileName = "/any-path-to-file/any-file.wav";
        String actualConvertedAudioFileName = audioConverterService.convertFileToWavIfNeeded(audioFileName);
        assertThat(actualConvertedAudioFileName, is(expectedConvertedAudioFileName));
    }

    @Test(expected = UnsupportedAudioFileException.class)
    public void audioFileCouldNotBeConverted() throws UnsupportedAudioFileException {
        when(AudioUtils.convertAudioFile(anyString(), anyString())).thenReturn(false);
        String audioFileName = "/any-path-to-file/any-file.mp3";
        String expectedConvertedAudioFileName = "/any-path-to-file/any-file.wav";
        String actualConvertedAudioFileName = audioConverterService.convertFileToWavIfNeeded(audioFileName);
        assertThat(actualConvertedAudioFileName, is(expectedConvertedAudioFileName));
    }

    @Test(expected = InternalServerErrorException.class)
    public void errorWhenConvertingFile() throws UnsupportedAudioFileException {
        when(AudioUtils.convertAudioFile(anyString(), anyString())).thenThrow(new InternalServerErrorException(new Exception()));
        String audioFileName = "/any-path-to-file/any-file.mp3";
        String expectedConvertedAudioFileName = "/any-path-to-file/any-file.wav";
        String actualConvertedAudioFileName = audioConverterService.convertFileToWavIfNeeded(audioFileName);
        assertThat(actualConvertedAudioFileName, is(expectedConvertedAudioFileName));
    }

}
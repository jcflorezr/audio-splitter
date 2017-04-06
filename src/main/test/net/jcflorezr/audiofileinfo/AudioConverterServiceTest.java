package net.jcflorezr.audiofileinfo;

import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.util.AudioUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.sound.sampled.UnsupportedAudioFileException;

import static org.junit.Assert.assertEquals;
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
        String audioFileName = "/any-path-to-file/any-file.mp3";
        String convertedAudioFileName = "/any-path-to-file/any-file.wav";
        when(AudioUtils.convertAudioFile(anyString(), anyString())).thenReturn(true);
        assertEquals(convertedAudioFileName, audioConverterService.convertFileToWavIfNeeded(audioFileName));
    }

    @Test
    public void audioFileFormatIsAlreadyWav() throws UnsupportedAudioFileException {
        String audioFileName = "/any-path-to-file/any-file.wav";
        String convertedAudioFileName = "/any-path-to-file/any-file.wav";
        when(AudioUtils.convertAudioFile(anyString(), anyString())).thenReturn(true);
        assertEquals(convertedAudioFileName, audioConverterService.convertFileToWavIfNeeded(audioFileName));
    }

    @Test(expected = UnsupportedAudioFileException.class)
    public void audioFileCouldNotBeConverted() throws UnsupportedAudioFileException {
        String audioFileName = "/any-path-to-file/any-file.wav";
        String convertedAudioFileName = "/any-path-to-file/any-file.wav";
        when(AudioUtils.convertAudioFile(anyString(), anyString())).thenReturn(false);
        assertEquals(convertedAudioFileName, audioConverterService.convertFileToWavIfNeeded(audioFileName));
    }

    @Test(expected = InternalServerErrorException.class)
    public void errorWhenConvertingFile() throws UnsupportedAudioFileException {
        String audioFileName = "/any-path-to-file/any-file.wav";
        String convertedAudioFileName = "/any-path-to-file/any-file.wav";
        when(AudioUtils.convertAudioFile(anyString(), anyString())).thenThrow(new InternalServerErrorException(new Exception()));
        assertEquals(convertedAudioFileName, audioConverterService.convertFileToWavIfNeeded(audioFileName));
    }

}
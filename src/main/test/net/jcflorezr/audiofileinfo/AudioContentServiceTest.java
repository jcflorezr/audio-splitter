package net.jcflorezr.audiofileinfo;

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.request.AudioFileLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class AudioContentServiceTest {

    private String testResourcesPath;

    @Mock
    private AudioIo audioIo;
    @InjectMocks
    private AudioContentService audioContentService;

    @Before
    public void setUp() {
        testResourcesPath = ClassLoader.class.getResource("/").getPath();
    }

    @Test
    public void retrieveAudioContent() throws Exception {
        String audioFileName = testResourcesPath + "test-audio-mono-22050.wav";
        String convertedAudioFileName = testResourcesPath + "test-audio-mono-22050.wav";
        AudioFileInfo dummyAudioFileInfo = createDummyAudioFileInfo(audioFileName, convertedAudioFileName);
        AudioSignal dummyAudioSignal = createDummyAudioSignal();

        when(audioIo.loadWavFile(anyString())).thenReturn(dummyAudioSignal);

        AudioContent actual = audioContentService.retrieveAudioContent(dummyAudioFileInfo);
        System.out.println();
    }

    private AudioFileInfo createDummyAudioFileInfo(String audioFileName, String convertedAudioFileName) {
        AudioFileLocation audioFileLocation = new AudioFileLocation(audioFileName, null);
        AudioFileInfo audioFileInfo = new AudioFileInfo(audioFileLocation);
        audioFileInfo.setConvertedAudioFileName(convertedAudioFileName);
        return audioFileInfo;
    }

    private AudioSignal createDummyAudioSignal() {
        int samplingRate = 44100;
        float[][] signalData = new float[][]{};
        return new AudioSignal(samplingRate, signalData);
    }

}
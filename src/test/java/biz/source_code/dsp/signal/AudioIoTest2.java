package biz.source_code.dsp.signal;

import biz.source_code.dsp.model.AudioSignal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

// TODO: remove it
public class AudioIoTest2 {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String testResourcesPath;
    private Class<? extends AudioIoTest2> thisClass;

    @Before
    public void setUp() {
        thisClass = this.getClass();
        testResourcesPath = thisClass.getResource("/audiocontent/signal/").getPath();
    }

    @Test
    public void retrieveAudioSignalFromWavFile() throws IOException, UnsupportedAudioFileException {

        String audioFileName = testResourcesPath + "with-applause/with-applause.wav";

        AudioIo2 audioIo = new AudioIo2();
        AudioSignal audioSignal = audioIo.retrieveAudioSignalFromWavFile(audioFileName);

        new ObjectMapper().writeValue(new File(testResourcesPath + "mysignal5.json"), audioSignal.getData());

//        AudioSignal expectedAudioClipSignal = MAPPER.readValue(thisClass.getResourceAsStream("/audiocontent/mysignal2.json"), AudioSignal.class);

//        assertThat(audioSignal, is(expectedAudioClipSignal));

    }

    @Test
    public void saveAudioFile() throws IOException, UnsupportedAudioFileException {

        String audioFileName = "/home/florez/Desktop/audio/signal/24_3";

        AudioIo2 audioIo = new AudioIo2();
        AudioSignal audioSignal = MAPPER.readValue(new File("/home/florez/Desktop/audio/signal/" + "24_3.json"), AudioSignal.class);
        audioIo.saveAudioFile(audioFileName, ".flac", audioSignal);

//        new ObjectMapper().writeValue(new File(testResourcesPath + "mysignal3.json"), audioSignal.getSignal());

//        AudioSignal expectedAudioClipSignal = MAPPER.readValue(thisClass.getResourceAsStream("/audiocontent/mysignal2.json"), AudioSignal.class);

//        assertThat(audioSignal, is(expectedAudioClipSignal));

    }

}
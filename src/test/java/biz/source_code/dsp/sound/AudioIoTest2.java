package biz.source_code.dsp.sound;

import biz.source_code.dsp.model.AudioSignal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

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

        String audioFileName = testResourcesPath + "strong-background-noise";

        AudioIo2 audioIo = new AudioIo2();
        AudioSignal audioSignal = MAPPER.readValue(new File(testResourcesPath + "strong-background-noise-signal.json"), AudioSignal.class);
        audioIo.saveAudioFile(audioFileName, ".wav", audioSignal);

//        new ObjectMapper().writeValue(new File(testResourcesPath + "mysignal3.json"), audioSignal.getData());

//        AudioSignal expectedAudioClipSignal = MAPPER.readValue(thisClass.getResourceAsStream("/audiocontent/mysignal2.json"), AudioSignal.class);

//        assertThat(audioSignal, is(expectedAudioClipSignal));

    }

}
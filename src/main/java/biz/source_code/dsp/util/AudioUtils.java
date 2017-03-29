package biz.source_code.dsp.util;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

import java.io.InputStream;
import java.text.DecimalFormat;

public class AudioUtils {

    public static void convertAudioFile(String originalAudioFilePath, String newAudioFilePath) {
        try {
            Converter converter = new Converter();
            converter.convert(originalAudioFilePath, newAudioFilePath);
        } catch (JavaLayerException e) {
            // TODO implementar una mejor manera de gestionar esta excepcion
            throw new RuntimeException(e);
        }
    }

    public static void convertAudioFile(InputStream originalAudioFileStream, String newAudioFilePath) {
        try {
            Converter converter = new Converter();
            converter.convert(originalAudioFileStream, newAudioFilePath, null, null);

            // TODO: check if new audio was created

        } catch (JavaLayerException e) {
            // TODO implementar una mejor manera de gestionar esta excepcion
            throw new RuntimeException(e);
        }
    }

    public static float formatAudioTimeWithMilliseconds(float audioTime) {
        return new Float(new DecimalFormat(".000").format(audioTime));
    }

}

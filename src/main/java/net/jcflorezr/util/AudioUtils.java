package net.jcflorezr.util;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class AudioUtils {

    public static boolean convertAudioFile(String originalAudioFilePath, String newAudioFilePath) {
        try {
            Converter converter = new Converter();
            converter.convert(originalAudioFilePath, newAudioFilePath);
            return Files.exists(Paths.get(newAudioFilePath));
        } catch (JavaLayerException e) {
            // TODO implementar una mejor manera de gestionar esta excepcion
            throw new RuntimeException(e);
        }
    }

    public static float formatAudioTimeWithMilliseconds(float audioTime) {
        return new Float(new DecimalFormat(".000").format(audioTime));
    }

}

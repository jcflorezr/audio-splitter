package net.jcflorezr.audiofileinfo;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.util.AudioUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;

class AudioConverterService {

    private static final Tika TIKA = new Tika();
    private static final String WAV = "wav";
    private static Predicate<String> isWav = fileName -> WAV.contains(fileName.toLowerCase());

    String convertFileToWavIfNeeded(String audioFileName) throws UnsupportedAudioFileException {
        Optional<String> convertedAudioFileName =
                ofNullable(audioFileName)
                        .filter(fileName -> audioFileNeedsToBeConverted(fileName))
                        .map(fileName -> FilenameUtils.removeExtension(fileName).concat(".wav"));
        return convertedAudioFileName
                .filter(convertedFileName -> AudioUtils.convertAudioFile(audioFileName, convertedFileName))
                .orElseThrow(() -> new UnsupportedAudioFileException("The file '" + audioFileName + "' could not be converted to wav."));
    }

    private boolean audioFileNeedsToBeConverted(String audioFileName) {
        String audioFileMimeType = TIKA.detect(audioFileName);
        String audioFileExtension = AudioFormatsSupported.getExtension(audioFileMimeType);
        return !isWav.test(audioFileExtension);
    }

}

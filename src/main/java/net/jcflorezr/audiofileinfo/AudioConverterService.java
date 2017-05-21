package net.jcflorezr.audiofileinfo;

import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.util.AudioUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.Optional;
import java.util.function.BiPredicate;

import static java.util.Optional.ofNullable;

@Service
class AudioConverterService {

    private static final Tika TIKA = new Tika();
    private static final String WAV = "wav";
    static BiPredicate<String, String> compareExtensions = (extension1, extension2) ->
            extension1.contains(extension2.toLowerCase()) || extension2.contains(extension1.toLowerCase());

    String convertFileToWavIfNeeded(String audioFileName) throws UnsupportedAudioFileException {
        Optional<String> convertedAudioFileName =
                ofNullable(audioFileName)
                        .filter(fileName -> audioFileNeedsToBeConverted(fileName))
                        .map(fileName -> FilenameUtils.removeExtension(fileName).concat(".wav"));
        if (!convertedAudioFileName.isPresent()) {
            return audioFileName;
        }
        return convertedAudioFileName
                .filter(convertedFileName -> AudioUtils.convertAudioFile(audioFileName, convertedFileName))
                .orElseThrow(() -> new UnsupportedAudioFileException("The file '" + audioFileName + "' could not be converted to wav."));
    }

    private boolean audioFileNeedsToBeConverted(String audioFileName) {
        String audioFileMimeType = TIKA.detect(audioFileName);
        String audioFileExtension = AudioFormatsSupported.getExtension(audioFileMimeType);
        return !compareExtensions.test(WAV, audioFileExtension);
    }

}

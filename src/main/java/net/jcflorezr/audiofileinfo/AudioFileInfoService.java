package net.jcflorezr.audiofileinfo;

import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.exceptions.SeparatorAudioFileNotFoundException;
import net.jcflorezr.model.AudioContent;
import net.jcflorezr.model.AudioFileInfo;
import net.jcflorezr.model.AudioFileLocation;
import net.jcflorezr.model.AudioMetadata;
import biz.source_code.dsp.model.AudioSignal;
import net.jcflorezr.model.GroupAudioSoundZonesInfo;
import net.jcflorezr.audioclips.SoundZonesDetector;
import biz.source_code.dsp.sound.AudioIo;
import biz.source_code.dsp.util.AudioFormatsSupported;
import net.jcflorezr.util.AudioUtils;
import net.jcflorezr.util.JsonUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

public class AudioFileInfoService {

    private AudioIo audioIo = new AudioIo();

    private static final Tika TIKA = new Tika();
    //    private static final String PROPERTIES_FILE = "/audioFiles.properties";
    // TODO I should create global variables with Spring @Value annotation
    private static final String SEPARATOR_FILE_NAME = "/files/separator2channels44100.wav";
    // TODO I should create global variables with Spring @Value annotation
    private static final String RESAMPLED_SEPARATOR_FILE_NAME = "/resampledSeparator.wav";
    private static final String WAV = "wav";

    private static Predicate<String> isWav = fileName -> WAV.contains(fileName.toLowerCase());
    private static BiPredicate<AudioSignal, AudioSignal> separatorAudioFileNeedsToBeResampled =
            (originalAudioSignal, separatorAudioSignal) ->
                    (originalAudioSignal.getSamplingRate() != separatorAudioSignal.getSamplingRate());

    public AudioFileInfo generateAudioFileInfo(AudioFileLocation audioFileLocation, boolean grouped) throws Exception {
        AudioFileInfo audioFileInfo = new AudioFileInfo(audioFileLocation);
        try {
            audioFileInfo.setConvertedAudioFileName(convertFileToWavIfNeeded(audioFileLocation.getAudioFileName()));

            AudioContent audioContent = new AudioContent();
            AudioSignal audioSignal = audioIo.loadWavFile(audioFileInfo.getConvertedAudioFileName());
            audioContent.setOriginalAudioSignal(audioSignal);

            SoundZonesDetector soundZonesDetector = new SoundZonesDetector(audioSignal);
            audioFileInfo.setSingleAudioSoundZones(soundZonesDetector.getAudioSoundZones());

            if (grouped) {
                audioContent.setSeparatorAudioSignal(getSeparatorAudioSignal(audioSignal));
                List<GroupAudioSoundZonesInfo> groupedAudioFileSoundZones =
                        soundZonesDetector.getGroupedAudioFileSoundZones(audioFileInfo.getSingleAudioSoundZones());
                audioFileInfo.setGroupedAudioFileSoundZones(groupedAudioFileSoundZones);
            }
            audioContent.setAudioMetadata(extractAudioMetadata(audioFileLocation.getAudioFileName()));
            audioFileInfo.setAudioContent(audioContent);
            return audioFileInfo;
        } finally {
            if (audioFileInfo.audioFileWasConverted()) {
                Path fileConvertedPath = Paths.get(audioFileInfo.getConvertedAudioFileName());
                Files.delete(fileConvertedPath);
            }
        }
    }

    private String convertFileToWavIfNeeded(String audioFileName) throws UnsupportedAudioFileException {
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

    private AudioMetadata extractAudioMetadata(String audioFileName) throws TikaException, SAXException, IOException {
        try (InputStream inputstream = new FileInputStream(new File(audioFileName))) {
            Metadata metadata = new Metadata();
            BodyContentHandler bodyContentHandler = new BodyContentHandler();
            new Mp3Parser().parse(inputstream, bodyContentHandler, metadata, new ParseContext());
            Map<String, String> metadataMap = Stream.of(metadata.names()).collect(toMap(name -> name, name -> metadata.get(name)));
            AudioMetadata audioMetadata = JsonUtils.convertMapToPojo(metadataMap, AudioMetadata.class);
            List<String> rawMetadata = Arrays.asList(split(bodyContentHandler.toString(), "\n"));
            if (isBlank(audioMetadata.getComments())) {
                audioMetadata.setComments(retrieveFileCommentsFromHandler(rawMetadata));
            }
            audioMetadata.setRawMetadata(rawMetadata);
            return audioMetadata;
        }
    }

    // This method retrieve the comments assuming that there is a dash (-) in it
    private String retrieveFileCommentsFromHandler(List<String> rawMetadata) {
        return rawMetadata.stream()
                .filter(element -> element.contains("-"))
                .filter(element -> countMatches(element, "-") > 1)
                .findFirst().orElse(StringUtils.EMPTY);
    }

    private AudioSignal getSeparatorAudioSignal(AudioSignal originalAudioSignal) throws Exception {
        InputStream separatorStream = Optional.ofNullable(ClassLoader.class.getResourceAsStream(SEPARATOR_FILE_NAME))
                .orElseThrow(() -> new SeparatorAudioFileNotFoundException());
        AudioSignal separatorAudioSignal = audioIo.loadWavFile(separatorStream);
        if (separatorAudioFileNeedsToBeResampled.test(originalAudioSignal, separatorAudioSignal)) {
            try {
                separatorAudioSignal = audioIo.resampleWavFile(originalAudioSignal, separatorAudioSignal, RESAMPLED_SEPARATOR_FILE_NAME);
            } catch (IOException e) {
                throw new InternalServerErrorException(e);
            }
        }
        return separatorAudioSignal;
    }

}

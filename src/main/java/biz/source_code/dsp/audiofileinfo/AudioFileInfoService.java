package biz.source_code.dsp.audiofileinfo;

import biz.source_code.dsp.exceptions.SeparatorAudioFileNotFoundException;
import biz.source_code.dsp.model.*;
import biz.source_code.dsp.signal.SoundZonesDetector;
import biz.source_code.dsp.sound.AudioIo;
import biz.source_code.dsp.util.AudioFormatsSupported;
import biz.source_code.dsp.util.AudioUtils;
import biz.source_code.dsp.util.JsonUtils;
import org.apache.commons.io.FilenameUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class AudioFileInfoService {

    private AudioIo audioIo = new AudioIo();

//    private static final String PROPERTIES_FILE = "/audioFiles.properties";
    private static final String SEPARATOR_FILE_NAME = "/files/separator2channels44100.wav";
    private static final String RESAMPLED_SEPARATOR_FILE_NAME = "/files/resampledSeparator.wav";
    private static final Tika TIKA = new Tika();
    private static final String WAV = "wav";

    private static Predicate<String> isWav = fileName -> WAV.contains(fileName.toLowerCase());
    private static BiPredicate<AudioSignal, AudioSignal> separatorAudioFileNeedsToBeResampled =
            (originalAudioSignal, separatorAudioSignal) ->
                    (originalAudioSignal.getSamplingRate() != separatorAudioSignal.getSamplingRate());

//    public AudioFilesConfigProperties getAudioFilesConfigProperties() {
//        // TODO I should create global variables with Spring @Value annotation
//        Properties properties = new Properties();
//        try {
//            // TODO I should create global variables with Spring @Value annotation
//            properties.load(ClassLoader.class.getResourceAsStream(PROPERTIES_FILE));
//            return new AudioFilesConfigProperties(properties);
//        } catch (IOException e) {
//            throw new InternalServerErrorException(e);
//        }
//    }

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
            new Mp3Parser().parse(inputstream, new BodyContentHandler(), metadata, new ParseContext());
            return JsonUtils.convertMapToPojo(getRawMetadata(metadata), AudioMetadata.class);
        }
    }

    private Map<String, String> getRawMetadata(Metadata metadata) {
        return Stream.of(metadata.names())
                .collect(Collectors.toMap(name -> name, name -> metadata.get(name)));
    }

    private AudioSignal getSeparatorAudioSignal(AudioSignal originalAudioSignal) throws Exception {
        InputStream separatorStream = Optional.ofNullable(ClassLoader.class.getResourceAsStream(SEPARATOR_FILE_NAME))
                .orElseThrow(() -> new SeparatorAudioFileNotFoundException());
        AudioSignal separatorAudioSignal = audioIo.loadWavFile(separatorStream);
        if (separatorAudioFileNeedsToBeResampled.test(originalAudioSignal, separatorAudioSignal)) {
            separatorAudioSignal = audioIo.resampleWavFile(originalAudioSignal, separatorAudioSignal, RESAMPLED_SEPARATOR_FILE_NAME);
        }
        return separatorAudioSignal;
    }

//    private String getGroupSeparatorAudioName(AudioSignal originalAudioSignal) throws IOException {
//        // TODO I should create global variables with Spring @Value annotation for these properties
//        Properties properties = new Properties();
//        properties.load(ClassLoader.class.getResourceAsStream(PROPERTIES_FILE));
//
//
//        if (isAudioCDStereo.test(originalAudioSignal.getSamplingRate(), originalAudioSignal.getChannels())) {
//            return properties.getProperty("audioFileGroupSeparator2Channels44100");
//        } // To add more group separator audio files cases
//        else {
//            return null;
//        }
//    }

    //    private static void p() throws IOException, GeneralSecurityException {
//        String STORAGE_SCOPE =
//                "https://www.googleapis.com/auth/devstorage.read_write";
//        GoogleCredential credential = GoogleCredential.getApplicationDefault().createScoped(Collections.singleton(STORAGE_SCOPE));
//        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
//        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
//        String bucketName = "idmji-152021.appspot.com";
//        String uri = "https://storage.googleapis.com/"
//                + URLEncoder.encode(bucketName, "UTF-8");
//        GenericUrl url = new GenericUrl(uri);
//        HttpRequest request = requestFactory.buildGetRequest(url);
//        HttpResponse response = request.execute();
//        System.out.println(response.parseAsString());
//    }

}

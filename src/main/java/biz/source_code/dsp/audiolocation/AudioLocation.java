package biz.source_code.dsp.audiolocation;

import biz.source_code.dsp.model.AudioFileInfo;
import biz.source_code.dsp.model.AudioFilesConfigProperties;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.model.GroupAudioSoundZonesInfo;
import biz.source_code.dsp.signal.SoundZonesDetector;
import biz.source_code.dsp.sound.AudioIo;
import biz.source_code.dsp.util.AudioFormatsSupported;
import biz.source_code.dsp.util.AudioUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import static biz.source_code.dsp.util.DateTimeUtils.getCurrentDateTime;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.getName;

public class AudioLocation {


    // TODO how to get the user folder of current OS



    private static final String PROPERTIES_FILE = "/audioFiles.properties";
    private static final Tika TIKA = new Tika();
    private static final String WAV = "wav";

    private static Predicate<String> isWav = fileName -> WAV.contains(fileName.toLowerCase());


    public AudioFilesConfigProperties getAudioFilesConfigProperties() throws IOException {
        // TODO I should create global variables with Spring @Value annotation
        Properties properties = new Properties();
        properties.load(ClassLoader.class.getResourceAsStream(PROPERTIES_FILE));
        return new AudioFilesConfigProperties(properties);
    }

    public List<String> getAudioFilesNamesInsideInputDirectory(String directory) throws IOException {
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            return StreamSupport.stream(directoryStream.spliterator(), false)
                    .filter(audioFilePath -> AudioFormatsSupported.isAudioFormatSupported(audioFilePath))
                    .map(audioFilePath -> audioFilePath.toString())
                    .collect(toList());
        }
    }

    public AudioFileInfo generateAudioFileInfo(String audioFileName, AudioFilesConfigProperties configProperties, boolean grouped) throws Exception {

        String convertedAudioFileName = convertFileToWavIfNeeded(audioFileName);

        String outputFileDirectoryPath = configProperties.getOutputFilesDirectoryPath();
        String outputFileDirectoryPathWithFileName = createDirectoryWithAudioName(audioFileName, outputFileDirectoryPath);

        AudioSignal audioSignal = AudioIo.loadWavFile(convertedAudioFileName);
        SoundZonesDetector soundZonesDetector = new SoundZonesDetector(audioSignal);

        AudioFileInfo audioFileInfo = new AudioFileInfo();
        audioFileInfo.setOriginalAudioFileName(audioFileName);
        audioFileInfo.setConvertedAudioFileName(convertedAudioFileName);
        audioFileInfo.setOutputFilesDirectoryPathWithFileName(outputFileDirectoryPathWithFileName);
        audioFileInfo.setAudioDurationInSeconds(audioSignal.getLength() / audioSignal.getSamplingRate());
        audioFileInfo.setAudioSignal(audioSignal);
        audioFileInfo.setSingleAudioFileSoundZones(soundZonesDetector.getAudioFileSoundZones());
//        audioFileInfo.setAudioFilesConfigProperties(configProperties);
        if (grouped) {
            List<GroupAudioSoundZonesInfo> groupedAudioFileSoundZones =
                    soundZonesDetector.getGroupedAudioFileSoundZones(audioFileInfo.getSingleAudioFileSoundZones());
            audioFileInfo.setGroupedAudioFileSoundZones(groupedAudioFileSoundZones);
            audioFileInfo.setSeparatorAudioSignal(getSeparatorAudioSignal(audioSignal));
        }
        if (audioFileInfo.audioFileWasConverted()) {
            Path fileConvertedPath = Paths.get(audioFileInfo.getConvertedAudioFileName());
            Files.delete(fileConvertedPath);
        }
        return audioFileInfo;
    }

    private AudioSignal getSeparatorAudioSignal(AudioSignal audioSignal) throws Exception {
        // TODO I should create global variables with Spring @Value annotation
        Properties properties = new Properties();
        properties.load(ClassLoader.class.getResourceAsStream(PROPERTIES_FILE));
        int samplingRate = audioSignal.getSamplingRate();
        int channels = audioSignal.getChannels();
        String groupSeparatorAudioFileName = null;
        if (samplingRate == 44100 && channels == 2) {
            groupSeparatorAudioFileName = properties.getProperty("audioFileGroupSeparator2Channels44100");
        } // To add more group separator audio files cases
        if (groupSeparatorAudioFileName != null) {
            return AudioIo.loadWavFile(groupSeparatorAudioFileName);
        }
        throw new UnsupportedAudioFileException("Could not found a group separator audio file with '" + channels + "' channels and " +
                "sampling rate of '" + samplingRate + "'.");
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

    private String createDirectoryWithAudioName(String audioName, String outputFilePath) throws IOException {
        Path newDirectoryPath = Paths.get(outputFilePath + getName(FilenameUtils.removeExtension(audioName)));
        if (!Files.exists(newDirectoryPath)) {
            Files.createDirectory(newDirectoryPath);
        }
        String pathSeparator = newDirectoryPath.getFileSystem().getSeparator();
        newDirectoryPath = Paths.get(newDirectoryPath.toString() + pathSeparator + getCurrentDateTime("yyyyMMdd_HHmmss"));
        Files.createDirectory(newDirectoryPath);
        return newDirectoryPath.toString() + pathSeparator;
    }

    private void getMetadata(String audioFileName) {
        Parser parser = new Mp3Parser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = null;
        try {


            // TODO I need to find the way to not load the audio file more than once in memory

            inputstream = new FileInputStream(new File(audioFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ParseContext context = new ParseContext();

        try {
            parser.parse(inputstream, handler, metadata, context);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }
        System.out.println(handler.toString());

        //getting the list of all meta data elements
        String[] metadataNames = metadata.names();

        for(String name : metadataNames) {
            System.out.println(name + ": " + metadata.get(name));
        }
    }

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

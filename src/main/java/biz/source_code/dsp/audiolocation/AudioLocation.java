package biz.source_code.dsp.audiolocation;

import biz.source_code.dsp.model.AudioFileInfo;
import biz.source_code.dsp.model.AudioFilesConfigProperties;
import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.signal.SoundZonesDetector;
import biz.source_code.dsp.sound.AudioIo;
import biz.source_code.dsp.util.AudioFormatsSupported;
import biz.source_code.dsp.util.AudioUtils;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.function.BiPredicate;
import java.util.stream.StreamSupport;

import static biz.source_code.dsp.util.AudioFormatsSupported.getExtension;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.*;

public class AudioLocation {


    // TODO how to get the user folder of current OS



    private static final String PROPERTIES_FILE = "/audioFiles.properties";
    private static final Tika TIKA = new Tika();

    private static BiPredicate<String, String> compareAudioExtension =
            (fileName, extension) -> getExtension(fileName).toLowerCase().contains(extension);


    public static AudioFilesConfigProperties getAudioFilesConfigProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(ClassLoader.class.getResourceAsStream(PROPERTIES_FILE));
        return new AudioFilesConfigProperties(properties);
    }

    public static List<String> getAudioFilesNamesInsideInputDirectory(String directory) throws IOException {
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            return StreamSupport.stream(directoryStream.spliterator(), false)
                    .filter(path -> isAudioFileSupported(path))
                    .map(path -> path.toString())
                    .collect(toList());
        }
    }

    public static AudioFileInfo generateAudioFileInfo(String audioFileName, AudioFilesConfigProperties configProperties, boolean grouped) throws Exception {
        String convertedAudioFileName = AudioLocation.convertFileToWavIfNeeded(Paths.get(audioFileName));

        String outputFileDirectoryPath = configProperties.getOutputFilesDirectoryPath();
        String outputFileDirectoryPathWithFileName = AudioLocation.createDirectoryWithAudioName(audioFileName, outputFileDirectoryPath);

        AudioSignal audioSignal = AudioIo.loadWavFile(convertedAudioFileName);
        SoundZonesDetector soundZonesDetector = new SoundZonesDetector(audioSignal);

        AudioFileInfo audioFileInfo = new AudioFileInfo();
        audioFileInfo.setOriginalAudioFileName(audioFileName);
        audioFileInfo.setConvertedAudioFileName(convertedAudioFileName);
        audioFileInfo.setOutputFilesDirectoryPathWithFileName(outputFileDirectoryPathWithFileName);
        audioFileInfo.setAudioDurationInSeconds(audioSignal.getLength() / audioSignal.getSamplingRate());
        audioFileInfo.setAudioSignal(audioSignal);
        audioFileInfo.setSingleAudioFileSoundZones(soundZonesDetector.getAudioFileSoundZones());
        audioFileInfo.setAudioFilesConfigProperties(configProperties);
        if (grouped) {
            audioFileInfo.setGroupedAudioFileSoundZones(soundZonesDetector.
                    getGroupedAudioFileSoundZones(audioFileInfo.getSingleAudioFileSoundZones()));
        }
        return audioFileInfo;
    }

    private static String convertFileToWavIfNeeded(Path path) throws UnsupportedAudioFileException {
        String originalAudioFilePath = path.toString();


        Parser parser = new Mp3Parser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = null;
        try {
            inputstream = new FileInputStream(new File(originalAudioFilePath));
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










        String originalAudioMimeType = TIKA.detect(originalAudioFilePath);
        String originalAudioExtension = getExtension(originalAudioMimeType);
        if (compareAudioExtension.test(originalAudioExtension, "wav")) {
            return originalAudioFilePath;
        }
        String wavAudioFilePath = removeExtension(originalAudioFilePath).concat(".wav");
        AudioUtils.convertAudioFile(originalAudioFilePath, wavAudioFilePath);
        return wavAudioFilePath;
    }

    private static String createDirectoryWithAudioName(String audioName, String outputFilePath) throws IOException {
        Path newDirectoryPath = Paths.get(outputFilePath + getName(removeExtension(audioName)));
        if (!Files.exists(newDirectoryPath)) {
            Files.createDirectory(newDirectoryPath);
        }
        String pathSeparator = newDirectoryPath.getFileSystem().getSeparator();
        newDirectoryPath = Paths.get(newDirectoryPath.toString() + pathSeparator + getCurrentDateTime());
        Files.createDirectory(newDirectoryPath);
        return newDirectoryPath.toString() + pathSeparator;
    }

    private static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return now.format(formatter);
    }

    private static boolean isAudioFileSupported(Path path) {
        String mimeType = TIKA.detect(path.toString());
        return AudioFormatsSupported.isAudioFormatSupported(mimeType);
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

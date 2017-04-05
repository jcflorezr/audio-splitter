package net.jcflorezr.audiofileinfo;

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileInfo;
import net.jcflorezr.model.audiocontent.AudioMetadata;
import net.jcflorezr.model.request.AudioFileLocation;
import net.jcflorezr.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;


public class AudioContentService {

    private AudioIo audioIo = new AudioIo();

    AudioContent retrieveAudioContent(AudioFileInfo audioFileInfo, AudioFileLocation audioFileLocation) throws IOException, UnsupportedAudioFileException, TikaException, SAXException {
        AudioContent audioContent = new AudioContent();
        AudioSignal originalAudioSignal = retrieveOriginalAudioSignal(audioFileInfo);
        audioContent.setOriginalAudioSignal(originalAudioSignal);
        audioContent.setAudioMetadata(extractAudioMetadata(audioFileLocation.getAudioFileName()));
        return audioContent;
    }

    private AudioSignal retrieveOriginalAudioSignal(AudioFileInfo audioFileInfo) throws IOException, UnsupportedAudioFileException {
        try {
            return audioIo.loadWavFile(audioFileInfo.getConvertedAudioFileName());
        } finally {
            if (audioFileInfo.audioFileWasConverted()) {
                Path fileConvertedPath = Paths.get(audioFileInfo.getConvertedAudioFileName());
                Files.deleteIfExists(fileConvertedPath);
            }
        }
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

}

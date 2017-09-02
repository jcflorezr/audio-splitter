package net.jcflorezr.audiofileinfo;

import biz.source_code.dsp.model.AudioSignal;
import biz.source_code.dsp.sound.AudioIo;
import net.jcflorezr.model.audiocontent.AudioContent;
import net.jcflorezr.model.audiocontent.AudioFileCompleteInfo;
import net.jcflorezr.model.audiocontent.AudioFileMetadata;
import net.jcflorezr.model.persistence.AudioFileNamePrimaryKey;
import net.jcflorezr.util.JsonUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.sound.sampled.UnsupportedAudioFileException;
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
import static org.apache.commons.lang3.StringUtils.split;

@Service
class AudioContentService {

    private AudioIo audioIo = new AudioIo();

    AudioContent retrieveAudioContent(AudioFileCompleteInfo audioFileCompleteInfo) throws IOException, UnsupportedAudioFileException, TikaException, SAXException {
        AudioSignal originalAudioSignal = retrieveOriginalAudioSignal(audioFileCompleteInfo);
        AudioFileMetadata audioFileMetadata = extractAudioMetadata(audioFileCompleteInfo.getAudioFileBasicInfo().getAudioFileName());
        return new AudioContent(originalAudioSignal, audioFileMetadata);
    }

    private AudioSignal retrieveOriginalAudioSignal(AudioFileCompleteInfo audioFileCompleteInfo) throws IOException, UnsupportedAudioFileException {
        String convertedAudioFileName = audioFileCompleteInfo.getAudioFileBasicInfo().getConvertedAudioFileName();
        try {
            return audioIo.retrieveAudioSignalFromWavFile(convertedAudioFileName);
        } finally {
            boolean convertedAudioFileExists = Files.exists(Paths.get(convertedAudioFileName));
            if (audioFileCompleteInfo.getAudioFileBasicInfo().audioFileWasConverted() && convertedAudioFileExists) {
                Path fileConvertedPath = Paths.get(convertedAudioFileName);
                Files.deleteIfExists(fileConvertedPath);
            }
        }
    }

    private AudioFileMetadata extractAudioMetadata(String audioFileName) throws TikaException, SAXException, IOException {
        try (InputStream inputstream = new FileInputStream(audioFileName)) {
            Metadata metadata = new Metadata();
            BodyContentHandler bodyContentHandler = new BodyContentHandler();
            new AutoDetectParser().parse(inputstream, bodyContentHandler, metadata, new ParseContext());
            Map<String, Object> metadataMap = Stream.of(metadata.names()).collect(toMap(name -> name, name -> metadata.get(name)));
            metadataMap.put("audioFileNamePrimaryKey", new AudioFileNamePrimaryKey(audioFileName));
            AudioFileMetadata audioFileMetadata = JsonUtils.convertMapToPojo(metadataMap, AudioFileMetadata.class);
            List<String> rawMetadata = Arrays.asList(split(bodyContentHandler.toString(), "\n"));
            audioFileMetadata.setRawMetadata(rawMetadata);
            return audioFileMetadata;
        }
    }

}

package net.jcflorezr.audiocontent;

import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.split;

@Service
class AudioContentService {

//    private AudioIo audioIo = new AudioIo();
//
//    AudioContent retrieveAudioContent(AudioFileCompleteInfo audioFileCompleteInfo) throws IOException, UnsupportedAudioFileException, TikaException, SAXException {
//        AudioSignal originalAudioSignal = retrieveOriginalAudioSignal(audioFileCompleteInfo);
//        AudioFileMetadataEntity audioFileMetadataEntity = extractAudioMetadata(audioFileCompleteInfo.getAudioFileBasicInfoEntity().getAudioFileName());
//        return new AudioContent(originalAudioSignal, audioFileMetadataEntity);
//    }
//
//    private AudioSignal retrieveOriginalAudioSignal(AudioFileCompleteInfo audioFileCompleteInfo) throws IOException, UnsupportedAudioFileException {
//        String convertedAudioFileName = audioFileCompleteInfo.getAudioFileBasicInfoEntity().getConvertedAudioFileName();
//        try {
//
//
//            //aqui inicia el command
//
//
//            return audioIo.retrieveAudioSignalFromWavFile(convertedAudioFileName);
//        } finally {
//            boolean convertedAudioFileExists = Files.exists(Paths.get(convertedAudioFileName));
//            if (audioFileCompleteInfo.getAudioFileBasicInfoEntity().audioFileWasConverted() && convertedAudioFileExists) {
//                Path fileConvertedPath = Paths.get(convertedAudioFileName);
//                Files.deleteIfExists(fileConvertedPath);
//            }
//        }
//    }
//
//    private AudioFileMetadataEntity extractAudioMetadata(String audioFileName) throws TikaException, SAXException, IOException {
//        try (InputStream inputstream = new FileInputStream(audioFileName)) {
//            Metadata metadata = new Metadata();
//            BodyContentHandler bodyContentHandler = new BodyContentHandler();
//            new AutoDetectParser().parse(inputstream, bodyContentHandler, metadata, new ParseContext());
//            Map<String, Object> metadataMap = Stream.of(metadata.names()).collect(toMap(entityName -> entityName, entityName -> metadata.get(entityName)));
//            metadataMap.put("audioFileNamePrimaryKey", new AudioFileNamePrimaryKey(audioFileName));
//            AudioFileMetadataEntity audioFileMetadataEntity = JsonUtils.convertMapToPojo(metadataMap, AudioFileMetadataEntity.class);
//            List<String> rawMetadata = Arrays.asList(split(bodyContentHandler.toString(), "\n"));
//            audioFileMetadataEntity.setRawMetadata(rawMetadata);
//            return audioFileMetadataEntity;
//        }
//    }

}

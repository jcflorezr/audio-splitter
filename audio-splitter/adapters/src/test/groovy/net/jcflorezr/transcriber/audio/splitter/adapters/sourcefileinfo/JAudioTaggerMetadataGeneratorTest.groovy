package net.jcflorezr.transcriber.audio.splitter.adapters.sourcefileinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.jcflorezr.transcriber.audio.splitter.adapters.util.SupportedAudioFormats
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileMetadata
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class JAudioTaggerMetadataGeneratorTest extends Specification {

    private JAudioTaggerMetadataGenerator jAudioTaggerMetadataGenerator = new JAudioTaggerMetadataGenerator()

    private static Class<? extends JAudioTaggerMetadataGeneratorTest> thisClass = this.class
    private static ObjectMapper mapper = new ObjectMapper().registerModule(new KotlinModule())

    def @Shared mp3AudioFileMetadata = "Retrieve metadata from MP3 audio file"
    def @Shared flacAudioFileMetadata = "Retrieve metadata from FLAC audio file"
    def @Shared wavAudioFileMetadata = "Retrieve metadata from WAV audio file"

    @Unroll
    def "#testTitle"() {

        when:
        def actualAudioMetadata = jAudioTaggerMetadataGenerator.retrieveAudioFileMetadata(new File(audioFilePath))

        then:
        def expectedAudioMetadata = mapper.readValue(new File(audioMetadataPath), AudioSourceFileMetadata.class)
        actualAudioMetadata == expectedAudioMetadata

        where:
        audioFormat                | audioFilePath                 | audioMetadataPath                 | testTitle
        SupportedAudioFormats.MP3  | getAudioFilePath(audioFormat) | getAudioMetadataPath(audioFormat) | mp3AudioFileMetadata
        SupportedAudioFormats.FLAC | getAudioFilePath(audioFormat) | getAudioMetadataPath(audioFormat) | flacAudioFileMetadata
        SupportedAudioFormats.WAV  | getAudioFilePath(audioFormat) | getAudioMetadataPath(audioFormat) | wavAudioFileMetadata

    }

    private static String getAudioFilePath(SupportedAudioFormats audioFormat) {
        thisClass.getResource("/source-file-info/test-audio-mono.$audioFormat.extension").getPath()
    }

    private static String getAudioMetadataPath(SupportedAudioFormats audioFormat) {
        thisClass.getResource("/source-file-info/audio-file-metadata-${audioFormat.extension}.json").getPath()
    }
}

package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.jcflorezr.transcriber.audio.splitter.domain.exception.AudioSourceException
import spock.lang.Specification
import spock.lang.Unroll

class AudioContentInfoTest extends Specification {

    private static Class<AudioContentInfoTest> thisClass = this.class
    private static String testResourcesPath = thisClass.getResource("/source-file-info").getPath()
    private static String expectedAudioContentFileName = "audio-content-info.json"
    private static ObjectMapper mapper = new ObjectMapper().registerModule(new KotlinModule())

    @Unroll
    def "Extract audio content info from WAV file"() {

        given:
        def audioFileName = "test-audio-mono.wav"

        when:
        def actualAudioContentInfo = extractAudioContentInfo(audioFileName)

        then:
        def expectedAudioContentInfo = getExpectedAudioContentInfo()
        verifyAll { assert actualAudioContentInfo == expectedAudioContentInfo }
    }

    @Unroll
    def "Throw too short file exception"() {

        given:
        def audioFileName = "too-short-test-audio-mono.wav"

        when:
        extractAudioContentInfo(audioFileName)

        then:
        def actualException = thrown(AudioSourceException)
        def expectedException = new AudioSourceException.Companion().audioSourceTooShort()
        verifyAll {
            assert actualException.errorCode == expectedException.errorCode
            assert actualException.message == expectedException.message
        }
    }

    private static AudioContentInfo extractAudioContentInfo(String audioFileName) {
        String audioFilePath = "$testResourcesPath/$audioFileName"
        return new AudioContentInfo.Companion().extractFrom(new File(audioFilePath))
    }

    private static AudioContentInfo getExpectedAudioContentInfo() {
        String audioContentPath = "$testResourcesPath/$expectedAudioContentFileName"
        return mapper.readValue(new File(audioContentPath), AudioContentInfo.class)
    }
}
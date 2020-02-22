package net.jcflorezr.audio.splitter.domain.sourcefile

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import spock.lang.Specification
import spock.lang.Unroll

class AudioContentInfoTest extends Specification {

    private static Class<AudioContentInfoTest> thisClass = this.class
    private static String testResourcesPath = thisClass.getResource("/source-file").getPath()
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

    private static AudioContentInfo extractAudioContentInfo(String audioFileName) {
        String audioFilePath = "$testResourcesPath/$audioFileName"
        return new AudioContentInfo.Companion().create(new File(audioFilePath))
    }

    private static AudioContentInfo getExpectedAudioContentInfo() {
        String audioContentPath = "$testResourcesPath/$expectedAudioContentFileName"
        return mapper.readValue(new File(audioContentPath), AudioContentInfo.class)
    }
}
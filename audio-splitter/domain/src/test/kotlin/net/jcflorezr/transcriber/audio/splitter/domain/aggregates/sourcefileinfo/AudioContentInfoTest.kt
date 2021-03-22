package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import net.jcflorezr.transcriber.core.exception.AudioSourceException
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class AudioContentInfoTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioContentInfoTest> = AudioContentInfoTest::class.java
    private val testResourcesPath = thisClass.getResource("/source-file-info").path
    private val expectedAudioContentFileName = "audio-content-info.json"

    @Test
    @DisplayName("Extract audio content info from WAV file")
    fun extractAudioContentFromWavFile() {
        val audioFileName = "test-audio-mono.wav"
        assertThat(extractAudioContentInfo(audioFileName), Is(equalTo(getExpectedAudioContentInfo())))
    }

    @Test
    @DisplayName("Should throw too short file exception")
    fun shouldThrowTooShortFileException() {
        val audioFileName = "too-short-test-audio-mono.wav"
        val actualException = Assertions.assertThrows(AudioSourceException::class.java) {
            extractAudioContentInfo(audioFileName)
        }
        val expectedException = AudioSourceException.audioSourceTooShort()
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
    }

    private fun extractAudioContentInfo(audioFileName: String): AudioContentInfo {
        val audioFilePath = "$testResourcesPath/$audioFileName"
        return AudioContentInfo.extractFrom(File(audioFilePath))
    }

    private fun getExpectedAudioContentInfo(): AudioContentInfo {
        val audioContentPath = "$testResourcesPath/$expectedAudioContentFileName"
        return MAPPER.readValue(File(audioContentPath), AudioContentInfo::class.java)
    }
}

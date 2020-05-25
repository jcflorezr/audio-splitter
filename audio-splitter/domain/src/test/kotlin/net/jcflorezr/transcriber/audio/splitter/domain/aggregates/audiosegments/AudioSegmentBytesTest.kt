package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Test

internal class AudioSegmentBytesTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioSegmentBytesTest> = this.javaClass
    private val audioSegmentsPath: String

    init {
        audioSegmentsPath = thisClass.getResource("/audio-segments").path
    }

    @Test
    fun generateBytesForAudioSegment() {
        // Given
        val bytesArray = MAPPER.readValue(File("$audioSegmentsPath/audio-segment-bytes.json"), ByteArray::class.java)
        val subArraysSize = bytesArray.size / 10

        generateSequence(0) { subArrayStart ->

            // When
            val actualSubArray = AudioSegmentBytes.of(bytesArray, subArrayStart, subArrayStart + subArraysSize).bytes

            // Then
            val expectedSubArray = bytesArray.copyOfRange(subArrayStart, subArrayStart + subArraysSize)
            assertThat(actualSubArray, Is(equalTo(expectedSubArray)))

            subArrayStart + subArraysSize
        }
        .takeWhile { subArrayStart -> subArrayStart < bytesArray.size }
        .count()
    }
}

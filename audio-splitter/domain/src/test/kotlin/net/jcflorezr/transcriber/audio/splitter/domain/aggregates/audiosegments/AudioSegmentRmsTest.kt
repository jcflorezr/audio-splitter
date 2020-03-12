package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import org.hamcrest.CoreMatchers.`is` as Is

internal class AudioSegmentRmsTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioSegmentRmsTest> = this.javaClass
    private val audioSegmentsPath: String

    init {
        audioSegmentsPath = thisClass.getResource("/audio-segments").path
    }

    @Test
    fun generateAudioSegmentRmsFromAudioSignal() {
        // Given
        val signalType = MAPPER.typeFactory.constructCollectionType(List::class.java, Float::class.java)
        val audioSegmentSignal: List<Float> =
            MAPPER.readValue(File("$audioSegmentsPath/audio-segment-signal.json"), signalType)

        // When
        val actualAudioSegmentRms = AudioSegmentRms.createNew(signal = listOf(audioSegmentSignal)).rms

        // Then
        val expectedAudioSegmentRms = MAPPER.readValue(File("$audioSegmentsPath/audio-segment-rms.json"), Double::class.java)
        assertThat(actualAudioSegmentRms, Is(equalTo(expectedAudioSegmentRms)))
    }
}
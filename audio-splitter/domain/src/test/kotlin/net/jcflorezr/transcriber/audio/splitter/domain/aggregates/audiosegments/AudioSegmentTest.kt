package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.hamcrest.CoreMatchers.`is` as Is

import java.io.File

internal class AudioSegmentTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioSegmentTest> = this.javaClass
    private val audioSegmentsPath: String
    private val audioContentInfoPath: String

    init {
        audioSegmentsPath = thisClass.getResource("/audio-segments").path
        audioContentInfoPath = thisClass.getResource("/source-file-info").path
    }

    @Test
    fun generateAudioSegment() {
        // Given
        val bytes = MAPPER.readValue<ByteArray>(File("$audioSegmentsPath/audio-segment-bytes.json"))
        val signalType = MAPPER.typeFactory.constructCollectionType(List::class.java, Float::class.java)
        val audioSegmentSignal: List<Float> =
            MAPPER.readValue(File("$audioSegmentsPath/audio-segment-signal.json"), signalType)
        val audioContentInfo = MAPPER.readValue<AudioContentInfo>(File("$audioContentInfoPath/audio-content-info.json"))

        // When
        val actualAudioSegment = AudioSegment.createNew(
            segmentStart = 44100,
            audioFileName = "any-audio-fie-name",
            audioSegmentBytes = AudioSegmentBytes.of(bytes = bytes, from = 0, to = bytes.size),
            audioSegmentRms = AudioSegmentRms.createNew(signal = listOf(audioSegmentSignal)),
            audioContentInfo = audioContentInfo)

        // Then
        val expectedAudioSegment = MAPPER.readValue<AudioSegment>(File("$audioSegmentsPath/audio-segment.json"))
        assertThat(actualAudioSegment, Is(equalTo(expectedAudioSegment)))
    }
}
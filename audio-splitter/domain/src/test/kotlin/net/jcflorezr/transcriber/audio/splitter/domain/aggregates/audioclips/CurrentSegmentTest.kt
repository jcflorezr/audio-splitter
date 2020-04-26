package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class CurrentSegmentTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<CurrentSegmentTest> = this.javaClass
    private val audioSegmentsTestResourcesPath = thisClass.getResource("/audio-segments").path
    private val audioClipsTestResourcesPath = thisClass.getResource("/audio-clips").path
    private val audioSegmentsFileName = "basic-audio-segments.json"
    private val currentActiveSegmentsFileName = "current-active-segments.json"

    @Test
    fun createNewCurrentSegment() {
        val actualCurrentSegment = CurrentSegment.createNew()

        assertThat(actualCurrentSegment.silenceCounter, Is(equalTo(0)))
        assertThat(actualCurrentSegment.activeCounter, Is(equalTo(0)))
        assertThat(actualCurrentSegment.activeSegmentStart, Is(equalTo(0)))
        assertThat(actualCurrentSegment.activeSegmentEnd, Is(equalTo(0)))
        assertThat(actualCurrentSegment.previousRms, Is(equalTo(0.0)))
        assertThat(actualCurrentSegment.previousDifference, Is(equalTo(0.0)))
        assertFalse(actualCurrentSegment.segmentWithNoisyBackgroundDetected)
    }

    @Test
    fun assertCurrentSegments() {
        val newCurrentSegment = CurrentSegment.createNew()
        val segments = getAudioSegments()
        val currentActiveSegments = getCurrentActiveSegments()

        segments.foldIndexed(newCurrentSegment) { i, currentSegment, audioSegment ->
            currentSegment
                .also { segment ->
                    assertThat("assertion error in current segment number: $i",
                        segment, Is(equalTo(currentActiveSegments[i])))
                }
                .process(audioSegment, false)
        }
    }

    private fun getAudioSegments(): List<BasicAudioSegment> {
        val audioSegmentsPath = "$audioSegmentsTestResourcesPath/$audioSegmentsFileName"
        val audioSegmentsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, BasicAudioSegment::class.java)
        return MAPPER.readValue(File(audioSegmentsPath), audioSegmentsListType)
    }

    private fun getCurrentActiveSegments(): List<CurrentSegment> {
        val currentActiveSegmentsPath = "$audioClipsTestResourcesPath/$currentActiveSegmentsFileName"
        val currentActiveSegmentsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, CurrentSegment::class.java)
        return MAPPER.readValue(File(currentActiveSegmentsPath), currentActiveSegmentsListType)
    }
}

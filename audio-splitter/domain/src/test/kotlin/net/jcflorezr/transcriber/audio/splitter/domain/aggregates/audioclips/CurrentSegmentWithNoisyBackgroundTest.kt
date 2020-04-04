package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.io.File

internal class CurrentSegmentWithNoisyBackgroundTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<CurrentSegmentWithNoisyBackgroundTest> = this.javaClass
    private val audioSegmentsTestResourcesPath = thisClass.getResource("/audio-segments").path
    private val audioClipsTestResourcesPath = thisClass.getResource("/audio-clips").path
    private val audioSegmentsFileName = "basic-audio-segments.json"
    private val currentActiveSegmentsFileName = "current-active-segments-with-noisy-background.json"
    private val audioSegments = getAudioSegments()

    @Test
    fun createNewCurrentSegment() {
        val actualCurrentSegment =
            CurrentSegmentWithNoisyBackground.createNew(audioSegments = audioSegments, fromIndex = 0)

        assertThat(actualCurrentSegment.inactiveCounter, Is(equalTo(0)))
        assertThat(actualCurrentSegment.activeCounter, Is(equalTo(0)))
        assertThat(actualCurrentSegment.activeSegmentStart, Is(equalTo(0)))
        assertThat(actualCurrentSegment.activeSegmentEnd, Is(equalTo(0)))
        assertThat(actualCurrentSegment.previousRms, Is(equalTo(0.0)))
        assertThat(actualCurrentSegment.previousDifference, Is(equalTo(0.0)))
    }

    @Test
    fun assertCurrentSegments() {
        val newCurrentSegment =
            CurrentSegmentWithNoisyBackground.createNew(audioSegments = audioSegments, fromIndex = 5)
        val segments = getAudioSegments()
        val currentActiveSegments = getCurrentActiveSegments()

        segments.foldIndexed(newCurrentSegment) { i, currentSegment, audioSegment ->
            currentSegment
                .also { segment ->
                    assertThat("assertion error in current segment number: $i",
                        segment, Is(equalTo(currentActiveSegments[i])))
                }
                .process(audioSegment, i)
        }
    }

    private fun getAudioSegments(): List<BasicAudioSegment> {
        val audioSegmentsPath = "$audioSegmentsTestResourcesPath/$audioSegmentsFileName"
        val audioSegmentsListType =
            MAPPER.typeFactory.constructCollectionType(List::class.java, BasicAudioSegment::class.java)
        return MAPPER.readValue(File(audioSegmentsPath), audioSegmentsListType)
    }

    private fun getCurrentActiveSegments(): List<CurrentSegmentWithNoisyBackground> {
        val currentActiveSegmentsPath = "$audioClipsTestResourcesPath/$currentActiveSegmentsFileName"
        val currentActiveSegmentsListType =
            MAPPER.typeFactory.constructCollectionType(List::class.java, CurrentSegmentWithNoisyBackground::class.java)
        return MAPPER.readValue(File(currentActiveSegmentsPath), currentActiveSegmentsListType)
    }
}

package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.activesegment

import java.io.File
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Test

internal class SegmentInNoiseTest : SegmentTest() {

    private val segmentInNoise = initializeSegmentInNoise()

    @Test
    fun createNewCurrentSegment() {
        assertThat(segmentInNoise.inactiveCounter, Is(equalTo(0)))
        assertThat(segmentInNoise.activeCounter, Is(equalTo(0)))
        assertThat(segmentInNoise.activeSegmentStart, Is(equalTo(0)))
        assertThat(segmentInNoise.activeSegmentEnd, Is(equalTo(0)))
        assertThat(segmentInNoise.previousRms, Is(equalTo(0.0)))
        assertThat(segmentInNoise.previousDifference, Is(equalTo(0.0)))
    }

    @Test
    fun assertCurrentSegments() {
        assertCurrentSegments(segment = segmentInNoise, activeSegmentsFileName = "segments-in-noise.json")
    }

    private fun initializeSegmentInNoise(): SegmentInNoise =
        Segment.createNewSegmentInSilence(getAudioSegments(), getAudioContentInfo()).createNewSegmentInNoise(getAudioClips().first())

    private fun getAudioClips(): List<AudioClip> {
        val audioClipsPath = "$audioClipsTestResourcesPath/$audioClipsFileName"
        val audioClipsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClip::class.java)
        return MAPPER.readValue(File(audioClipsPath), audioClipsListType)
    }
}

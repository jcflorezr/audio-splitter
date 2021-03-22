package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.activesegment

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class SegmentInSilenceTest : SegmentTest() {

    private val segmentInSilence = Segment.createNewSegmentInSilence(getAudioSegments(), getAudioContentInfo())

    @Test
    fun createNewCurrentSegment() {
        assertThat(segmentInSilence.silenceCounter, Is(equalTo(0)))
        assertThat(segmentInSilence.activeCounter, Is(equalTo(0)))
        assertThat(segmentInSilence.activeSegmentStart, Is(equalTo(0)))
        assertThat(segmentInSilence.activeSegmentEnd, Is(equalTo(0)))
        assertThat(segmentInSilence.previousRms, Is(equalTo(0.0)))
        assertThat(segmentInSilence.previousDifference, Is(equalTo(0.0)))
        assertFalse(segmentInSilence.segmentWithNoisyBackgroundDetected)
    }

    @Test
    fun assertCurrentSegments() {
        assertCurrentSegments(segment = segmentInSilence, activeSegmentsFileName = "segments-in-silence.json")
    }
}

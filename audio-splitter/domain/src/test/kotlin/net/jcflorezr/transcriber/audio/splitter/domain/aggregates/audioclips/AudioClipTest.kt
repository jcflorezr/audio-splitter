package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips

import java.io.File
import net.jcflorezr.transcriber.core.util.JsonUtils.fromJsonToList
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AudioClipTest {

    private val thisClass: Class<AudioClipTest> = this.javaClass
    private val audioClipsTestResourcesPath = thisClass.getResource("/audio-clips").path
    private val audioSegmentsFileName = "active-segments.json"
    private val audioClipsFileName = "audio-clips.json"
    private val activeSegments = getActiveSegments()
    private val expectedAudioClips = getExpectedAudioClips()

    @Test
    fun createNewAudioClip() {
        val newAudioClip = AudioClip.createNew()

        assertThat(newAudioClip.duration, Is(equalTo(0.0f)))
        assertThat(newAudioClip.hours, Is(equalTo(0)))
        assertThat(newAudioClip.minutes, Is(equalTo(0)))
        assertThat(newAudioClip.seconds, Is(equalTo(0)))
        assertThat(newAudioClip.tenthsOfSecond, Is(equalTo(0)))
        assertFalse(newAudioClip.activeSegments.isNotEmpty())
    }

    @Test
    fun assertAudioClips() {
        activeSegments
            .foldIndexed(AudioClip.createNew()) { i, currentClip, activeSegment ->
                currentClip
                    .processActiveSegment(activeSegment)
                    .also { clip -> assertThat("assertion error in audio clip number: $i", clip, Is(equalTo(expectedAudioClips[i]))) }
            }
    }

    @Test
    fun reset() {
        val audioClip = AudioClip.createNew().processActiveSegment(currentSegment = activeSegments[0]).reset()

        assertThat(audioClip.duration, Is(equalTo(0.0f)))
        assertThat(audioClip.hours, Is(equalTo(0)))
        assertThat(audioClip.minutes, Is(equalTo(0)))
        assertThat(audioClip.seconds, Is(equalTo(0)))
        assertThat(audioClip.tenthsOfSecond, Is(equalTo(0)))
        assertFalse(audioClip.activeSegments.isNotEmpty())
        assertFalse(audioClip.isFlushed())
    }

    @Test
    fun flush() {
        val audioClip = AudioClip.createNew().processActiveSegment(currentSegment = activeSegments[0]).flush()

        assertThat(audioClip.duration, Is(equalTo(0.0f)))
        assertThat(audioClip.hours, Is(equalTo(0)))
        assertThat(audioClip.minutes, Is(equalTo(0)))
        assertThat(audioClip.seconds, Is(equalTo(0)))
        assertThat(audioClip.tenthsOfSecond, Is(equalTo(0)))
        assertFalse(audioClip.activeSegments.isNotEmpty())
        assertTrue(audioClip.isFlushed())
    }

    @Test
    fun finish() {
        val audioClip = AudioClip.createNew().processActiveSegment(currentSegment = activeSegments[0]).finish()

        assertThat(audioClip.duration, Is(equalTo(3.2f)))
        assertThat(audioClip.hours, Is(equalTo(0)))
        assertThat(audioClip.minutes, Is(equalTo(0)))
        assertThat(audioClip.seconds, Is(equalTo(0)))
        assertThat(audioClip.tenthsOfSecond, Is(equalTo(2)))
        assertTrue(audioClip.activeSegments.isNotEmpty())
        assertFalse(audioClip.isFlushed())
    }

    @Test
    fun testAudioClipFileNamesGenerated() {
        expectedAudioClips.forEach { audioClip ->
            val expectedAudioClipName =
                audioClip.activeSegments.takeIf { it.isEmpty() }?.let { "" }
                    ?: activeSegments.first().segmentStartInSeconds.toString().replace(".", "_")
            assertThat(audioClip.audioClipFileName(), Is(equalTo(expectedAudioClipName)))
        }
    }

    @Test
    fun clipNameForEmptyAudioClip() {
        val clipName = AudioClip.createNew().audioClipFileName()
        assertThat(clipName, Is(equalTo("")))
    }

    private fun getActiveSegments() =
        fromJsonToList<ActiveSegment>(File("$audioClipsTestResourcesPath/$audioSegmentsFileName"))

    private fun getExpectedAudioClips() =
        fromJsonToList<AudioClip>(File("$audioClipsTestResourcesPath/$audioClipsFileName"))
}

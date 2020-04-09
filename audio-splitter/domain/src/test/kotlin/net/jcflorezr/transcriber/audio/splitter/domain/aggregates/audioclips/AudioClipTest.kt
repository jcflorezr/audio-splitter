package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import java.io.File

internal class AudioClipTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

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
                    .also { clip ->
                        assertThat("assertion error in audio clip number: $i", clip, Is(equalTo(expectedAudioClips[i])))
                    }
                    .processActiveSegment(activeSegment)
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
                ?: activeSegments.first().segmentStart.toString().replace(".", "_")
            assertThat(audioClip.audioClipFileName(), Is(equalTo(expectedAudioClipName)))
        }
    }

    @Test
    fun clipNameForEmptyAudioClip() {
        val clipName = AudioClip.createNew().audioClipFileName()
        assertThat(clipName, Is(equalTo("")))
    }

    private fun getActiveSegments(): List<ActiveSegment> {
        val audioSegmentsPath = "$audioClipsTestResourcesPath/$audioSegmentsFileName"
        val audioSegmentsListType =
            MAPPER.typeFactory.constructCollectionType(List::class.java, ActiveSegment::class.java)
        return MAPPER.readValue(File(audioSegmentsPath), audioSegmentsListType)
    }

    private fun getExpectedAudioClips(): List<AudioClip> {
        val audioClipsPath = "$audioClipsTestResourcesPath/$audioClipsFileName"
        val audioClipsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClip::class.java)
        return MAPPER.readValue(File(audioClipsPath), audioClipsListType)
    }
}
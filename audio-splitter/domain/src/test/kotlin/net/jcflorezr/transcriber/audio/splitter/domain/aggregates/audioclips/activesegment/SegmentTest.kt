package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.activesegment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is

abstract class SegmentTest {

    companion object {
        val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<SegmentTest> = this.javaClass
    private val audioSegmentsTestResourcesPath = thisClass.getResource("/audio-segments").path
    protected val audioClipsTestResourcesPath: String = thisClass.getResource("/audio-clips").path
    private val audioFileInfoTestResourcesPath = thisClass.getResource("/source-file-info").path
    private val audioSegmentsFileName = "basic-audio-segments.json"
    private val audioContentInfoFileName = "audio-content-info.json"
    protected val audioClipsFileName = "audio-clips.json"

    fun assertCurrentSegments(segment: Segment, activeSegmentsFileName: String) {
        val currentActiveSegments = getActiveSegments(activeSegmentsFileName)
        segment.audioSegments.foldIndexed(segment) { i, currentSegment, _ ->
            currentSegment.also { segment ->
                assertThat("assertion error in segment number: ${i + 1}", segment, Is(equalTo(currentActiveSegments[i])))
            }.process()
        }
    }

    protected fun getAudioContentInfo(): AudioContentInfo {
        val audioFileInfoPath = "$audioFileInfoTestResourcesPath/$audioContentInfoFileName"
        return MAPPER.readValue(File(audioFileInfoPath), AudioContentInfo::class.java)
    }

    protected fun getAudioSegments(): List<BasicAudioSegment> {
        val audioSegmentsPath = "$audioSegmentsTestResourcesPath/$audioSegmentsFileName"
        val audioSegmentsListType =
            MAPPER.typeFactory.constructCollectionType(List::class.java, BasicAudioSegment::class.java)
        return MAPPER.readValue(File(audioSegmentsPath), audioSegmentsListType)
    }

    private fun getActiveSegments(activeSegmentsFileName: String): List<Segment> {
        val activeSegmentsPath = "$audioClipsTestResourcesPath/$activeSegmentsFileName"
        val activeSegmentsListType =
            MAPPER.typeFactory.constructCollectionType(List::class.java, Segment::class.java)
        return MAPPER.readValue(File(activeSegmentsPath), activeSegmentsListType)
    }
}

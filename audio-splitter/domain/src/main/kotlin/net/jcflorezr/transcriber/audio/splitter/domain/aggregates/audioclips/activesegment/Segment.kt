package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.activesegment

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.lang.AssertionError
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.ActiveSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "@class"
)
@JsonSubTypes(
    Type(value = SegmentInSilence::class, name = "SegmentInSilence"),
    Type(value = SegmentInNoise::class, name = "SegmentInNoise")
)
interface Segment {

    val audioSegments: List<BasicAudioSegment>
    val currentIndex: Int
    val activeSegmentStart: Int
    val activeSegmentEnd: Int
    val previousRms: Double
    val previousDifference: Double
    val audioContentInfo: AudioContentInfo
    val audioClip: AudioClip

    companion object {
        fun createNewSegmentInSilence(audioSegments: List<BasicAudioSegment>, audioContentInfo: AudioContentInfo) = SegmentInSilence(
            currentIndex = 0, silenceCounter = 0, activeCounter = 0, activeSegmentStart = 0, activeSegmentEnd = 0,
            previousRms = 0.0, previousDifference = 0.0, audioClip = AudioClip.createNew(), segmentWithNoisyBackgroundDetected = false,
            audioSegments = audioSegments,
            audioContentInfo = audioContentInfo
        )
    }

    private fun incrementIndex(): Segment =
        when (this) {
            is SegmentInSilence -> this.copy(currentIndex = currentIndex + 1)
            is SegmentInNoise -> this.copy(currentIndex = currentIndex + 1)
            else -> throw AssertionError("No valid instance class")
        }

    fun generateNextSegment(audioFileName: String, processedSegment: Segment): Segment =
        processedSegment.takeIf { activeSegmentDetected(it) }
            ?.run {
                val audioClip = audioClipForSegment(audioFileName, processedSegment)
                when (processedSegment) {
                    is SegmentInNoise -> processedSegment.copy(currentIndex = currentIndex + 1, activeSegmentEnd = 0, audioClip = audioClip)
                    is SegmentInSilence -> when (processedSegment.segmentWithNoisyBackgroundDetected) {
                        false -> processedSegment.copy(currentIndex = currentIndex + 1, activeSegmentEnd = 0, audioClip = audioClip)
                        true -> processedSegment.createNewSegmentInNoise(processedSegment.audioClip)
                    }
                    else -> throw AssertionError("No valid instance class")
                }
            } ?: processedSegment.incrementIndex()

    private fun activeSegmentDetected(processedSegment: Segment): Boolean =
        when (processedSegment) {
            is SegmentInSilence ->
                processedSegment.run { activeSegmentEnd > activeSegmentStart || segmentWithNoisyBackgroundDetected }
            else -> processedSegment.run { activeSegmentEnd > activeSegmentStart }
        }

    private fun audioClipForSegment(audioFileName: String, processedSegment: Segment): AudioClip {
        val activeSegment = processedSegment.run {
            ActiveSegment.createNew(audioFileName, activeSegmentStart, activeSegmentEnd, audioContentInfo)
        }
        return audioClip.processActiveSegment(activeSegment)
    }

    fun process(): Segment
}

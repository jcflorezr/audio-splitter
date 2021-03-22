package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.activesegment

import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.math.abs
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.core.util.FloatingPointUtils

/*
    Value Object
 */
data class SegmentInNoise(
    val activeCounter: Int,
    val inactiveCounter: Int,
    val fromIndex: Int,
    val toIndex: Int,
    val segmentInSilence: SegmentInSilence,
    override val audioSegments: List<BasicAudioSegment>,
    override val currentIndex: Int,
    override val activeSegmentStart: Int,
    override val activeSegmentEnd: Int,
    override val previousRms: Double,
    override val previousDifference: Double,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) override val audioContentInfo: AudioContentInfo,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) override val audioClip: AudioClip
) : Segment {

    companion object {
        private const val ACTIVE_THRESHOLD = 0.03
    }

    override fun process(): SegmentInNoise =
        generateNextSegment(
            audioFileName = audioSegments[currentIndex].sourceAudioFileName,
            processedSegment = processSegment(audioSegments[currentIndex])
        ) as SegmentInNoise

    private fun processSegment(audioSegment: BasicAudioSegment): SegmentInNoise {
        val (segmentLength, isLastSegment, currentDifference, isPossibleActive) = getSegmentInfo(audioSegment, currentIndex)
        val isPossibleActiveSegmentStart = isPossibleActive && !isLastSegment
        val (currentActiveCounter, currentInactiveCounter) = getCurrentCounters(isPossibleActiveSegmentStart)
        val (isPossibleActiveSegmentEnd, isActiveSegmentStart, activeSegmentDetected) =
            getConditions(isPossibleActiveSegmentStart, currentInactiveCounter, currentActiveCounter, isLastSegment)
        return this.copy(
            activeCounter = if (isPossibleActiveSegmentEnd) { 0 } else { currentActiveCounter },
            inactiveCounter = if (isPossibleActiveSegmentStart) { 0 } else { currentInactiveCounter },
            activeSegmentEnd = if (activeSegmentDetected) { audioSegment.segmentStart } else { activeSegmentEnd },
            previousRms = audioSegment.audioSegmentRms,
            previousDifference = currentDifference,
            activeSegmentStart =
            when (isActiveSegmentStart) {
                true ->
                    if (audioSegment.segmentStart > segmentLength * 4) {
                        audioSegment.segmentStart - segmentLength * 4
                    } else {
                        audioSegment.segmentStart
                    }
                false -> activeSegmentStart
            }
        )
    }

    private fun getConditions(
        isPossibleActiveSegmentStart: Boolean,
        currentInactiveCounter: Int,
        currentActiveCounter: Int,
        isLastSegment: Boolean
    ): Triple<Boolean, Boolean, Boolean> {
        val isPossibleActiveSegmentEnd = !isPossibleActiveSegmentStart && (currentInactiveCounter == 3 || isLastSegment)
        val isActiveSegmentStart = isPossibleActiveSegmentStart && currentActiveCounter == 3
        val activeSegmentDetected = isPossibleActiveSegmentEnd && currentActiveCounter >= 3
        return Triple(isPossibleActiveSegmentEnd, isActiveSegmentStart, activeSegmentDetected)
    }

    private fun getSegmentInfo(audioSegment: BasicAudioSegment, currentIndex: Int): SegmentInfoInNoisyBackground {
        val segmentLength = audioSegment.segmentEnd - audioSegment.segmentStart
        val isLastSegment = currentIndex == segmentLength - 1
        val currentDifference = FloatingPointUtils.millisecondsFormat(value = (previousRms - audioSegment.audioSegmentRms))
        val deepDifference = FloatingPointUtils.millisecondsFormat(value = (previousDifference - currentDifference))
        val isPossibleActive = abs(deepDifference) >= ACTIVE_THRESHOLD
        return SegmentInfoInNoisyBackground(segmentLength, isLastSegment, currentDifference, isPossibleActive)
    }

    private fun getCurrentCounters(isPossibleActiveSegmentStart: Boolean): Pair<Int, Int> {
        val currentActiveCounter = if (isPossibleActiveSegmentStart) { activeCounter + 1 } else { activeCounter }
        val currentInactiveCounter = if (isPossibleActiveSegmentStart) { inactiveCounter } else { inactiveCounter + 1 }
        return currentActiveCounter to currentInactiveCounter
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SegmentInNoise

        if (activeCounter != other.activeCounter) return false
        if (inactiveCounter != other.inactiveCounter) return false
        if (fromIndex != other.fromIndex) return false
        if (toIndex != other.toIndex) return false
        if (currentIndex != other.currentIndex) return false
        if (activeSegmentStart != other.activeSegmentStart) return false
        if (activeSegmentEnd != other.activeSegmentEnd) return false
        if (previousRms != other.previousRms) return false
        if (previousDifference != other.previousDifference) return false

        return true
    }

    override fun hashCode(): Int {
        var result = activeCounter
        result = 31 * result + inactiveCounter
        result = 31 * result + fromIndex
        result = 31 * result + toIndex
        result = 31 * result + currentIndex
        result = 31 * result + activeSegmentStart
        result = 31 * result + activeSegmentEnd
        result = 31 * result + previousRms.hashCode()
        result = 31 * result + previousDifference.hashCode()
        return result
    }

    override fun toString() =
        "SegmentInSilence(" +
            "inactiveCounter=$inactiveCounter, " +
            "activeCounter=$activeCounter, " +
            "currentIndex=$currentIndex, " +
            "activeSegmentStart=$activeSegmentStart, " +
            "activeSegmentEnd=$activeSegmentEnd, " +
            "previousRms=$previousRms, " +
            "previousDifference=$previousDifference)"

    private data class SegmentInfoInNoisyBackground(
        val segmentLength: Int,
        val isLastSegment: Boolean,
        val currentDifference: Double,
        val isPossibleActive: Boolean
    )
}

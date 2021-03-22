package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.activesegment

import kotlin.math.abs
import kotlin.math.max
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.core.util.FloatingPointUtils

/*
    Value Object
 */
data class SegmentInSilence(
    val silenceCounter: Int,
    val activeCounter: Int,
    override val audioSegments: List<BasicAudioSegment>,
    override val currentIndex: Int,
    override val activeSegmentStart: Int,
    override val activeSegmentEnd: Int,
    override val previousRms: Double,
    override val previousDifference: Double,
    override val audioContentInfo: AudioContentInfo,
    override val audioClip: AudioClip,
    val segmentWithNoisyBackgroundDetected: Boolean
) : Segment {

    companion object {
        private const val SILENCE_THRESHOLD = 0.001
        private const val MAX_ACTIVE_COUNTER = 80
    }

    override fun process(): Segment =
        generateNextSegment(
            audioFileName = audioSegments[currentIndex].sourceAudioFileName,
            processedSegment = processSegment(audioSegments[currentIndex])
        )

    fun createNewSegmentInNoise(audioClip: AudioClip): SegmentInNoise {
        val currentSegment = audioSegments[currentIndex]
        val fromIndex = activeSegmentStart / (currentSegment.segmentEnd - currentSegment.segmentStart)
        val previousFromIndex = if (fromIndex > 0) { fromIndex - 1 } else { 0 }
        val previousRms = audioSegments[previousFromIndex].audioSegmentRms
        val currentRms = audioSegments[fromIndex].audioSegmentRms
        val previousDifference = FloatingPointUtils.millisecondsFormat(value = previousRms - currentRms)
        return SegmentInNoise(
            activeCounter = 0, inactiveCounter = 0, fromIndex, toIndex = currentIndex,
            segmentInSilence = this.copy(segmentWithNoisyBackgroundDetected = false, currentIndex = currentIndex + 1, audioClip = audioClip.flush()),
            audioSegments, currentIndex = fromIndex, activeSegmentStart = 0, activeSegmentEnd = 0, previousRms,
            previousDifference, audioContentInfo, audioClip
        )
    }

    private fun processSegment(audioSegment: BasicAudioSegment): SegmentInSilence {
        val (currentDifference, isPossibleSilence, segmentLength) = getSegmentInfo(audioSegment)
        val (currentSilenceCounter, currentActiveCounter) = getCurrentCounters(isPossibleSilence)
        val (isActiveSegmentStart, activeSegmentDetected, isSegmentWithNoisyBackground, isPossibleActiveSegmentStart) =
            getCurrentConditions(isPossibleSilence, currentSilenceCounter, currentActiveCounter)
        return this.copy(
            activeSegmentStart = when {
                activeSegmentDetected -> max(activeSegmentStart - (segmentLength * 2), 0)
                isPossibleActiveSegmentStart -> audioSegment.segmentStart
                else -> activeSegmentStart
            },
            activeSegmentEnd = if (activeSegmentDetected) { audioSegment.segmentStart } else { activeSegmentEnd },
            segmentWithNoisyBackgroundDetected = isSegmentWithNoisyBackground,
            silenceCounter = currentSilenceCounter,
            activeCounter = if (isActiveSegmentStart) { 0 } else { currentActiveCounter },
            previousRms = audioSegment.audioSegmentRms,
            previousDifference = currentDifference
        )
    }

    private fun getSegmentInfo(audioSegment: BasicAudioSegment): Triple<Double, Boolean, Int> {
        val currentDifference = FloatingPointUtils.millisecondsFormat(value = (previousRms - audioSegment.audioSegmentRms))
        val isPossibleSilence = abs(currentDifference) <= SILENCE_THRESHOLD
        val segmentLength = audioSegment.segmentEnd - audioSegment.segmentStart
        return Triple(currentDifference, isPossibleSilence, segmentLength)
    }

    private fun getCurrentCounters(isPossibleSilence: Boolean): Pair<Int, Int> {
        val currentSilenceCounter = if (isPossibleSilence) { silenceCounter + 1 } else { 0 }
        val currentActiveCounter =
            if (!isPossibleSilence || (isPossibleSilence && currentSilenceCounter < 2)) { activeCounter + 1 } else { activeCounter }
        return currentSilenceCounter to currentActiveCounter
    }

    private fun getCurrentConditions(
        isPossibleSilence: Boolean,
        currentSilenceCounter: Int,
        currentActiveCounter: Int
    ): Conditions {
        val isLastSegment = currentIndex == audioSegments.size - 1
        val isActiveSegmentStart = (isPossibleSilence && currentSilenceCounter == 2) || isLastSegment
        return Conditions(
            isActiveSegmentStart = isActiveSegmentStart,
            activeSegmentDetected = isActiveSegmentStart && currentActiveCounter in 3 until MAX_ACTIVE_COUNTER,
            isSegmentWithNoisyBackground = isActiveSegmentStart && currentActiveCounter >= MAX_ACTIVE_COUNTER,
            isPossibleActiveSegmentStart =
            (isActiveSegmentStart && currentActiveCounter <= 2) || currentActiveCounter == 1
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SegmentInSilence

        if (silenceCounter != other.silenceCounter) return false
        if (activeCounter != other.activeCounter) return false
        if (currentIndex != other.currentIndex) return false
        if (activeSegmentStart != other.activeSegmentStart) return false
        if (activeSegmentEnd != other.activeSegmentEnd) return false
        if (previousRms != other.previousRms) return false
        if (previousDifference != other.previousDifference) return false
        if (segmentWithNoisyBackgroundDetected != other.segmentWithNoisyBackgroundDetected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = silenceCounter
        result = 31 * result + activeCounter
        result = 31 * result + currentIndex
        result = 31 * result + activeSegmentStart
        result = 31 * result + activeSegmentEnd
        result = 31 * result + previousRms.hashCode()
        result = 31 * result + previousDifference.hashCode()
        result = 31 * result + segmentWithNoisyBackgroundDetected.hashCode()
        return result
    }

    override fun toString() =
        "SegmentInSilence(" +
            "silenceCounter=$silenceCounter, " +
            "activeCounter=$activeCounter, " +
            "currentIndex=$currentIndex, " +
            "activeSegmentStart=$activeSegmentStart, " +
            "activeSegmentEnd=$activeSegmentEnd, " +
            "previousRms=$previousRms, " +
            "previousDifference=$previousDifference, " +
            "segmentWithNoisyBackgroundDetected=$segmentWithNoisyBackgroundDetected)"

    private data class Conditions(
        val isActiveSegmentStart: Boolean,
        val activeSegmentDetected: Boolean,
        val isSegmentWithNoisyBackground: Boolean,
        val isPossibleActiveSegmentStart: Boolean
    )
}

package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.core.util.FloatingPointUtils
import kotlin.math.abs
import kotlin.math.max

/*
    Entity
 */
data class ActiveSegment(
    val sourceAudioFileName: String,
    val segmentStart: Float,
    val segmentEnd: Float,
    val duration: Float,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int
) {

    companion object {

        fun createNew(
            sourceAudioFileName: String,
            segmentStart: Int,
            segmentEnd: Int,
            audioContentInfo: AudioContentInfo
        ): ActiveSegment {
            val segmentStartInSeconds = audioContentInfo.calculateSegmentPositionInSeconds(segmentStart)
            val segmentEndInSeconds = audioContentInfo.calculateSegmentPositionInSeconds(segmentEnd)
            return ActiveSegment(
                sourceAudioFileName = sourceAudioFileName,
                segmentStart = segmentStartInSeconds,
                segmentEnd = segmentEndInSeconds,
                duration = audioContentInfo.calculateAudioClipDuration(segmentStartInSeconds, segmentEndInSeconds),
                hours = segmentStartInSeconds.toInt() / 3600,
                minutes = segmentStartInSeconds.toInt() % 3600 / 60,
                seconds = segmentStartInSeconds.toInt() % 60,
                tenthsOfSecond = segmentStartInSeconds.toString().substringAfter(".").toInt())
        }

        private fun AudioContentInfo.calculateSegmentPositionInSeconds(segmentPosition: Int) =
            FloatingPointUtils.tenthsSecondsFormat(segmentPosition.toFloat() / sampleRate.toFloat()).toFloat()

        private fun AudioContentInfo.calculateAudioClipDuration(segmentStart: Float, segmentEnd: Float): Float =
            FloatingPointUtils.tenthsSecondsFormat((segmentEnd - segmentStart) + (audioSegmentLength / sampleRate)).toFloat()

    }
}

/*
    Value Object
 */
data class CurrentSegment(
    val silenceCounter: Int,
    val activeCounter: Int,
    val activeSegmentStart: Int,
    val activeSegmentEnd: Int,
    val previousRms: Double,
    val previousDifference: Double,
    val segmentWithNoisyBackgroundDetected: Boolean
) {

    companion object {

        private const val SILENCE_THRESHOLD = 0.001
        private const val MAX_ACTIVE_COUNTER = 80

        fun createNew() = CurrentSegment(
            silenceCounter = 0,
            activeCounter = 0,
            activeSegmentStart = 0,
            activeSegmentEnd = 0,
            previousRms = 0.0,
            previousDifference = 0.0,
            segmentWithNoisyBackgroundDetected = false)
    }

    fun process(audioSegment: BasicAudioSegment, isLastSegment: Boolean): CurrentSegment {
        val (currentDifference, isPossibleSilence, segmentLength) = getSegmentInfo(audioSegment)
        val (currentSilenceCounter, currentActiveCounter) = getCurrentCounters(isPossibleSilence)
        val (isActiveSegmentStart, activeSegmentDetected, isSegmentWithNoisyBackground, isPossibleActiveSegmentStart) =
            getCurrentConditions(isPossibleSilence, currentSilenceCounter, currentActiveCounter, isLastSegment)
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
            previousDifference = currentDifference)
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
            if (!isPossibleSilence || (isPossibleSilence && currentSilenceCounter < 2))
                { activeCounter + 1 } else { activeCounter }
        return currentSilenceCounter to currentActiveCounter
    }

    private fun getCurrentConditions(
        isPossibleSilence: Boolean,
        currentSilenceCounter: Int,
        currentActiveCounter: Int,
        isLastSegment: Boolean
    ): Conditions {
        val isActiveSegmentStart = (isPossibleSilence && currentSilenceCounter == 2) || isLastSegment
        return Conditions(
            isActiveSegmentStart = isActiveSegmentStart,
            activeSegmentDetected = isActiveSegmentStart && currentActiveCounter in 3 until MAX_ACTIVE_COUNTER,
            isSegmentWithNoisyBackground = isActiveSegmentStart && currentActiveCounter >= MAX_ACTIVE_COUNTER,
            isPossibleActiveSegmentStart =
                (isActiveSegmentStart && currentActiveCounter <= 2) || currentActiveCounter == 1)
    }
}

/*
    Value Object
 */
data class CurrentSegmentWithNoisyBackground(
    val activeCounter: Int,
    val inactiveCounter: Int,
    var activeSegmentStart: Int,
    val activeSegmentEnd: Int,
    val previousRms: Double,
    val previousDifference: Double
) {

    companion object {
        private const val ACTIVE_THRESHOLD = 0.03

        fun createNew(audioSegments: List<BasicAudioSegment>, fromIndex: Int): CurrentSegmentWithNoisyBackground {
            val previousFromIndex = if (fromIndex > 0) { fromIndex - 1 } else { 0 }
            val previousRms = audioSegments[previousFromIndex].audioSegmentRms
            val currentRms = audioSegments[fromIndex].audioSegmentRms
            val previousDifference = FloatingPointUtils.millisecondsFormat(value = previousRms - currentRms)
            return CurrentSegmentWithNoisyBackground(
                activeCounter = 0,
                inactiveCounter = 0,
                activeSegmentStart = 0,
                activeSegmentEnd = 0,
                previousRms = previousRms,
                previousDifference = previousDifference)
        }
    }

    fun process(audioSegment: BasicAudioSegment, currentIndex: Int): CurrentSegmentWithNoisyBackground {
        val (segmentLength, isLastSegment, currentDifference, isPossibleActive) =
            getSegmentInfo(audioSegment, currentIndex)
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
            })
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
}

/*
    Value Object
 */
private data class Conditions(
    val isActiveSegmentStart: Boolean,
    val activeSegmentDetected: Boolean,
    val isSegmentWithNoisyBackground: Boolean,
    val isPossibleActiveSegmentStart: Boolean)

/*
    Value Object
 */
private data class SegmentInfoInNoisyBackground(
    val segmentLength: Int,
    val isLastSegment: Boolean,
    val currentDifference: Double,
    val isPossibleActive: Boolean)
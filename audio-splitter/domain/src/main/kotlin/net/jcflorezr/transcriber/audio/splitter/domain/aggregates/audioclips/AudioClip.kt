package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips

import net.jcflorezr.transcriber.core.util.FloatingPointUtils
import net.jcflorezr.transcriber.core.domain.AggregateRoot

/*
    Entity (Aggregate Root)
 */
data class AudioClip(
    val sourceAudioFileName: String,
    val duration: Float,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int,
    val audioClipFileName: String,
    val activeSegments: List<ActiveSegment>,
    private val previousSegment: ActiveSegment? = null
) : AggregateRoot {

    companion object {
        fun createNew() = AudioClip(sourceAudioFileName = "", duration = 0.0f, hours = 0, minutes = 0, seconds = 0,
            tenthsOfSecond = 0, audioClipFileName = "", activeSegments = listOf(), previousSegment = null)
    }

    fun reset() = this.copy(hours = 0, minutes = 0, seconds = 0, tenthsOfSecond = 0, duration = 0.0f,
        activeSegments = listOf())

    fun flush() = this.copy(hours = 0, minutes = 0, seconds = 0, tenthsOfSecond = 0, duration = 0.0f,
        activeSegments = listOf(), previousSegment = null)

    fun isFlushed() = hours == 0 && minutes == 0 && seconds == 0 && tenthsOfSecond == 0 &&
        duration == 0.0f && activeSegments.isEmpty() && previousSegment == null

    fun finish(): AudioClip {
        val currentActiveSegments = previousSegment?.let { activeSegments + it } ?: activeSegments
        val firstSegment = currentActiveSegments.first()
        val lastSegment = currentActiveSegments.last()
        return this.copy(
            hours = firstSegment.hours,
            minutes = firstSegment.minutes,
            seconds = firstSegment.seconds,
            tenthsOfSecond = firstSegment.tenthsOfSecond,
            duration = FloatingPointUtils.tenthsSecondsFormat(
                lastSegment.segmentEndInSeconds - firstSegment.segmentStartInSeconds).toFloat(),
            activeSegments = currentActiveSegments,
            previousSegment = null)
    }

    fun processActiveSegment(currentSegment: ActiveSegment): AudioClip {
        val currentActiveSegments = previousSegment?.let { activeSegments + it } ?: activeSegments
        if (currentActiveSegments.isEmpty()) {
             return this.copy(previousSegment = currentSegment)
        }
        val firstSegment = currentActiveSegments.first()
        val lastSegment = currentActiveSegments.last()
        val gap = FloatingPointUtils.tenthsSecondsFormat(currentSegment.segmentStartInSeconds - lastSegment.segmentEndInSeconds).toFloat()
            .takeIf { it > 0 } ?: 0.0f
        val previousDuration =
            FloatingPointUtils.tenthsSecondsFormat(lastSegment.segmentEndInSeconds - firstSegment.segmentStartInSeconds).toFloat()
        return when {
            gap.isEnoughForAudioClipCreation(previousDuration) ->
                AudioClip(
                    sourceAudioFileName = firstSegment.sourceAudioFileName,
                    hours = firstSegment.hours,
                    minutes = firstSegment.minutes,
                    seconds = firstSegment.seconds,
                    tenthsOfSecond = firstSegment.tenthsOfSecond,
                    duration = previousDuration,
                    activeSegments = currentActiveSegments,
                    previousSegment = currentSegment,
                    audioClipFileName = audioClipFileName().takeIf { it.isNotBlank() } ?: firstSegment.audioClipFileName())
            else -> this.copy(activeSegments = currentActiveSegments, previousSegment = currentSegment)
        }
    }

    fun audioClipFileName(): String = activeSegments
        .takeIf { it.isEmpty() }
        ?.let { "" }
        ?: activeSegments.first().audioClipFileName()

    private fun ActiveSegment.audioClipFileName() = segmentStartInSeconds.toString().replace(".", "_")

    private fun Float.isEnoughForAudioClipCreation(previousDuration: Float) =
        this > 3 ||
        this > 0.5f && previousDuration > 5 ||
        this == 0.5f && previousDuration > 6 ||
        this == 0.4f && previousDuration > 7 ||
        this == 0.3f && previousDuration > 8 ||
        this == 0.2f && previousDuration > 10

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioClip

        if (duration != other.duration) return false
        if (hours != other.hours) return false
        if (minutes != other.minutes) return false
        if (seconds != other.seconds) return false
        if (tenthsOfSecond != other.tenthsOfSecond) return false
        if (activeSegments != other.activeSegments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = duration.hashCode()
        result = 31 * result + hours
        result = 31 * result + minutes
        result = 31 * result + seconds
        result = 31 * result + tenthsOfSecond
        result = 31 * result + activeSegments.hashCode()
        return result
    }
}
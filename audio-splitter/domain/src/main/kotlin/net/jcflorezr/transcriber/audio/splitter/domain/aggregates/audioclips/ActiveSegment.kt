package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.core.util.FloatingPointUtils

/*
    Entity
 */
data class ActiveSegment(
    val sourceAudioFileName: String,
    val segmentStartInSeconds: Float,
    val segmentEndInSeconds: Float,
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
                segmentStartInSeconds = segmentStartInSeconds,
                segmentEndInSeconds = segmentEndInSeconds,
                duration = audioContentInfo.calculateAudioClipDuration(segmentStartInSeconds, segmentEndInSeconds),
                hours = segmentStartInSeconds.toInt() / 3600,
                minutes = segmentStartInSeconds.toInt() % 3600 / 60,
                seconds = segmentStartInSeconds.toInt() % 60,
                tenthsOfSecond = segmentStartInSeconds.toString().substringAfter(".").toInt()
            )
        }

        private fun AudioContentInfo.calculateSegmentPositionInSeconds(segmentPosition: Int) =
            FloatingPointUtils.tenthsSecondsFormat(segmentPosition.toFloat() / sampleRate.toFloat()).toFloat()

        private fun AudioContentInfo.calculateAudioClipDuration(segmentStart: Float, segmentEnd: Float): Float =
            FloatingPointUtils.tenthsSecondsFormat((segmentEnd - segmentStart) + (audioSegmentLength / sampleRate)).toFloat()
    }
}

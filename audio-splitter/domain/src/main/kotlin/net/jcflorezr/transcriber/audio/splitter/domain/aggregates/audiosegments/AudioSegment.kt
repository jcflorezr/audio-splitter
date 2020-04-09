package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.util.AudioUtils
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import kotlin.math.sqrt

/*
    Entity (Aggregate Root)
 */
data class AudioSegment private constructor(
    val sourceAudioFileName: String,
    val segmentStart: Int,
    val segmentStartInSeconds: Float,
    val segmentEnd: Int,
    val segmentEndInSeconds: Float,
    val audioSegmentRms: AudioSegmentRms,
    val audioSegmentBytes: AudioSegmentBytes
) : AggregateRoot {

    companion object {

        fun createNew(
            segmentStart: Int,
            sourceAudioFileName: String,
            audioContentInfo: AudioContentInfo,
            audioSegmentRms: AudioSegmentRms,
            audioSegmentBytes: AudioSegmentBytes
        ): AudioSegment {
            val sampleRate = audioContentInfo.sampleRate
            val framesToRead = audioSegmentBytes.bytes.size / audioContentInfo.frameSize
            return AudioSegment(
                sourceAudioFileName = sourceAudioFileName,
                segmentStart = segmentStart,
                segmentStartInSeconds = calculateSegmentStartInSeconds(segmentStart, sampleRate),
                segmentEnd = segmentStart + framesToRead,
                segmentEndInSeconds = calculateSegmentEndInSeconds(segmentStart, framesToRead, sampleRate),
                audioSegmentRms = audioSegmentRms,
                audioSegmentBytes = audioSegmentBytes)
        }

        private fun calculateSegmentStartInSeconds(segmentStart: Int, sampleRate: Int) =
            AudioUtils.tenthsSecondsFormat(segmentStart.toFloat() / sampleRate.toFloat()).toFloat()

        private fun calculateSegmentEndInSeconds(segmentStart: Int, framesToRead: Int, sampleRate: Int) =
            AudioUtils.tenthsSecondsFormat((segmentStart + framesToRead).toFloat() / sampleRate.toFloat()).toFloat()
    }
}

/*
    Value Object
 */
data class AudioSegmentBytes private constructor(val bytes: ByteArray) {

    companion object {

        fun of(bytes: ByteArray, from: Int, to: Int) = AudioSegmentBytes(bytes.copyOfRange(from, to))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioSegmentBytes

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}

/*
    Value Object
 */
data class AudioSegmentRms private constructor(val rms: Double) {

    companion object {
        fun createNew(signal: List<List<Float>>) =
            AudioSegmentRms(rms = signal[0].calculateSegmentRms())

        private fun List<Float>.calculateSegmentRms() =
            AudioUtils.millisecondsFormat(
                value = sqrt(fold(0.0) { a, b -> a + (b * b) }.toDouble() / size))
    }
}

/*
    Value Object
 */
data class BasicAudioSegment private constructor(
    val sourceAudioFileName: String,
    val segmentStart: Int,
    val segmentStartInSeconds: Float,
    val segmentEnd: Int,
    val segmentEndInSeconds: Float,
    val audioSegmentRms: Double
) {
    companion object {
        fun fromAudioSegment(audioSegment: AudioSegment) =
            BasicAudioSegment(
                sourceAudioFileName = audioSegment.sourceAudioFileName,
                segmentStart = audioSegment.segmentStart,
                segmentStartInSeconds = audioSegment.segmentStartInSeconds,
                segmentEnd = audioSegment.segmentEnd,
                segmentEndInSeconds = audioSegment.segmentEndInSeconds,
                audioSegmentRms = audioSegment.audioSegmentRms.rms)
    }
}
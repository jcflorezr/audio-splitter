package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments

import kotlin.math.sqrt
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.util.AudioBytesUnPacker
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.util.FloatingPointUtils

/*
    Entity (Aggregate Root)
 */
data class AudioSegment(
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
                audioSegmentBytes = audioSegmentBytes
            )
        }

        private fun calculateSegmentStartInSeconds(segmentStart: Int, sampleRate: Int) =
            FloatingPointUtils.tenthsSecondsFormat(segmentStart.toFloat() / sampleRate.toFloat()).toFloat()

        private fun calculateSegmentEndInSeconds(segmentStart: Int, framesToRead: Int, sampleRate: Int) =
            FloatingPointUtils.tenthsSecondsFormat((segmentStart + framesToRead).toFloat() / sampleRate.toFloat()).toFloat()

        fun extractAudioSegmentByteArray(
            segmentStart: Int,
            byteArray: ByteArray,
            audioContentInfo: AudioContentInfo
        ) = AudioSegmentBytes.of(
            bytes = byteArray,
            from = segmentStart * audioContentInfo.audioSegmentLengthInBytes,
            to = (segmentStart * audioContentInfo.audioSegmentLengthInBytes) + audioContentInfo.audioSegmentLengthInBytes
        )

        fun generateAudioSegmentSignal(
            segmentStart: Int,
            byteArray: ByteArray,
            audioContentInfo: AudioContentInfo
        ) = AudioBytesUnPacker.generateAudioSignal(
            audioContentInfo = audioContentInfo,
            bytesBuffer = byteArray,
            from = segmentStart * audioContentInfo.audioSegmentLength,
            to = (segmentStart * audioContentInfo.audioSegmentLength) + audioContentInfo.audioSegmentLength
        )
    }

    fun toBasicAudioSegment() = BasicAudioSegment(
        sourceAudioFileName, segmentStart, segmentStartInSeconds,
        segmentEnd, segmentEndInSeconds, audioSegmentRms.rms
    )
}

/*
    Value Object
 */
data class AudioSegmentBytes(val bytes: ByteArray) {

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
data class AudioSegmentRms(val rms: Double) {

    companion object {
        fun createNew(signal: List<List<Float>>) =
            AudioSegmentRms(rms = signal[0].calculateSegmentRms())

        private fun List<Float>.calculateSegmentRms() =
            FloatingPointUtils.millisecondsFormat(
                value = sqrt(fold(0.0) { a, b -> a + (b * b) }.toDouble() / size)
            )
    }
}

/*
    Aggregate Root
 */

data class BasicAudioSegments(val basicAudioSegments: List<BasicAudioSegment>) : AggregateRoot

/*
    Entity
 */
data class BasicAudioSegment(
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
                audioSegmentRms = audioSegment.audioSegmentRms.rms
            )
    }
}
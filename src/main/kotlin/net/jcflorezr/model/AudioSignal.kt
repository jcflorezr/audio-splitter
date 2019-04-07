package net.jcflorezr.model

import com.fasterxml.jackson.annotation.JsonIgnore
import net.jcflorezr.broker.Message
import net.jcflorezr.util.AudioUtils
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.nio.ByteBuffer

data class AudioSignal(
    val entityName: String = "audioSignal",
    val audioFileName: String,
    val index: Float,
    val sampleRate: Int,
    val totalFrames: Int,
    val initialPosition: Int,
    val initialPositionInSeconds: Float,
    val endPosition: Int,
    val endPositionInSeconds: Float,
    val data: Array<FloatArray?>,
    val dataInBytes: ByteArray,
    val audioSourceInfo: AudioSourceInfo
) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioSignal

        if (entityName != other.entityName) return false
        if (audioFileName != other.audioFileName) return false
        if (index != other.index) return false
        if (sampleRate != other.sampleRate) return false
        if (totalFrames != other.totalFrames) return false
        if (initialPosition != other.initialPosition) return false
        if (initialPositionInSeconds != other.initialPositionInSeconds) return false
        if (endPosition != other.endPosition) return false
        if (endPositionInSeconds != other.endPositionInSeconds) return false
        if (!data.contentDeepEquals(other.data)) return false
        if (!dataInBytes.contentEquals(other.dataInBytes)) return false
        if (audioSourceInfo != other.audioSourceInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entityName.hashCode()
        result = 31 * result + audioFileName.hashCode()
        result = 31 * result + index.hashCode()
        result = 31 * result + sampleRate
        result = 31 * result + totalFrames
        result = 31 * result + initialPosition
        result = 31 * result + initialPositionInSeconds.hashCode()
        result = 31 * result + endPosition
        result = 31 * result + endPositionInSeconds.hashCode()
        result = 31 * result + data.contentDeepHashCode()
        result = 31 * result + dataInBytes.contentHashCode()
        result = 31 * result + audioSourceInfo.hashCode()
        return result
    }
}

data class AudioSignalsRmsInfo(
    val audioSignals: List<AudioSignalRmsInfo>
) : Message

data class AudioSignalRmsInfo(
    val entityName: String = "audioSignalRms",
    val audioFileName: String,
    val index: Double,
    val rms: Double,
    val sampleRate: Int,
    val audioLength: Int,
    val initialPosition: Int,
    val initialPositionInSeconds: Float,
    val segmentSize: Int,
    val segmentSizeInSeconds: Float,
    val silence: Boolean,
    val active: Boolean
) {
    @JsonIgnore fun isLastSegment() = initialPosition + segmentSize == audioLength ||
        audioLength - (initialPosition + segmentSize) < segmentSize
}

@Table(value = "audio_signal_rms")
data class AudioSignalRmsEntity(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @PrimaryKeyColumn(name = "ind", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val index: Float,
    @Column("rms") val rms: Double,
    @Column("sample_size") val sampleSize: Int,
    @Column("audio_length") val audioLength: Int,
    @Column("initial_position") val initialPosition: Int,
    @Column("initial_position_in_seconds") val initialPositionInSeconds: Float,
    @Column("segment_size") val segmentSize: Int,
    @Column("sample_size_in_seconds") val segmentSizeInSeconds: Float,
    @Column("silence") val silence: Boolean,
    @Column("active") val active: Boolean
) {
    constructor(audioSignalRms: AudioSignalRmsInfo) :
        this(
            audioFileName = audioSignalRms.audioFileName,
            index = AudioUtils.tenthsSecondsFormat(audioSignalRms.index).toFloat(),
            rms = audioSignalRms.rms,
            sampleSize = audioSignalRms.sampleRate,
            audioLength = audioSignalRms.audioLength,
            initialPosition = audioSignalRms.initialPosition,
            initialPositionInSeconds = audioSignalRms.initialPositionInSeconds,
            segmentSize = audioSignalRms.segmentSize,
            segmentSizeInSeconds = audioSignalRms.segmentSizeInSeconds,
            silence = audioSignalRms.silence,
            active = audioSignalRms.active
        )
}

@Table(value = "audio_part")
data class AudioPartEntity(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @PrimaryKeyColumn(name = "ind", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val index: Float,
    @Column("channels") val channels: Int,
    @Column("sample_rate") val sampleRate: Int,
    @Column("sample_size_in_bits") val sampleSizeInBits: Int,
    @Column("sample_size") val sampleSize: Int,
    @Column("frame_size") val frameSize: Int,
    @Column("big_endian") val bigEndian: Boolean,
    @Column("encoding") val encoding: String,
    @Column("content") val content: ByteBuffer
) {
    constructor(audioSignal: AudioSignal) :
        this (
            audioFileName = audioSignal.audioFileName,
            index = audioSignal.index,
            channels = audioSignal.audioSourceInfo.channels,
            sampleRate = audioSignal.audioSourceInfo.sampleRate,
            sampleSizeInBits = audioSignal.audioSourceInfo.sampleSizeInBits,
            sampleSize = audioSignal.audioSourceInfo.sampleSize,
            frameSize = audioSignal.audioSourceInfo.frameSize,
            bigEndian = audioSignal.audioSourceInfo.bigEndian,
            encoding = audioSignal.audioSourceInfo.encoding.name,
            content = ByteBuffer.wrap(audioSignal.dataInBytes)
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioPartEntity

        if (audioFileName != other.audioFileName) return false
        if (index != other.index) return false
        if (channels != other.channels) return false
        if (sampleRate != other.sampleRate) return false
        if (sampleSizeInBits != other.sampleSizeInBits) return false
        if (sampleSize != other.sampleSize) return false
        if (frameSize != other.frameSize) return false
        if (bigEndian != other.bigEndian) return false
        if (encoding != other.encoding) return false

        val thisContentBytes = ByteArray(content.remaining())
        content.get(thisContentBytes)
        val otherContentBytes = ByteArray(other.content.remaining())
        other.content.get(otherContentBytes)
        if (!thisContentBytes.contentEquals(otherContentBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = audioFileName.hashCode()
        result = 31 * result + index.hashCode()
        result = 31 * result + channels
        result = 31 * result + sampleRate
        result = 31 * result + sampleSizeInBits
        result = 31 * result + sampleSize
        result = 31 * result + frameSize
        result = 31 * result + bigEndian.hashCode()
        result = 31 * result + encoding.hashCode()
        result = 31 * result + content.hashCode()
        return result
    }
}

data class AudioClipSignal(
    val sampleRate: Int,
    val signal: Array<FloatArray?>,
    val audioClipName: String,
    val audioFileName: String,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int,
    val initialPositionInSeconds: Float,
    val endPositionInSeconds: Float,
    val lastClip: Boolean
) : Message {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioClipSignal

        if (sampleRate != other.sampleRate) return false
        if (!signal.contentDeepEquals(other.signal)) return false
        if (audioClipName != other.audioClipName) return false
        if (audioFileName != other.audioFileName) return false
        if (hours != other.hours) return false
        if (minutes != other.minutes) return false
        if (seconds != other.seconds) return false
        if (tenthsOfSecond != other.tenthsOfSecond) return false
        if (initialPositionInSeconds != other.initialPositionInSeconds) return false
        if (endPositionInSeconds != other.endPositionInSeconds) return false
        if (lastClip != other.lastClip) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sampleRate
        result = 31 * result + signal.contentDeepHashCode()
        result = 31 * result + audioClipName.hashCode()
        result = 31 * result + audioFileName.hashCode()
        result = 31 * result + hours
        result = 31 * result + minutes
        result = 31 * result + seconds
        result = 31 * result + tenthsOfSecond
        result = 31 * result + initialPositionInSeconds.hashCode()
        result = 31 * result + endPositionInSeconds.hashCode()
        result = 31 * result + lastClip.hashCode()
        return result
    }
}
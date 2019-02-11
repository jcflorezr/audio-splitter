package net.jcflorezr.model

import biz.source_code.dsp.model.AudioSignalKt
import com.fasterxml.jackson.annotation.JsonIgnore
import net.jcflorezr.broker.Message
import net.jcflorezr.util.AudioUtilsKt
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.nio.ByteBuffer

data class AudioSignalsRmsInfo(
    val audioSignals: List<AudioSignalRmsInfoKt>
) : Message

// TODO: rename it when its java equivalent is removed
data class AudioSignalRmsInfoKt(
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
    constructor(audioSignalRms: AudioSignalRmsInfoKt) :
        this(
            audioFileName = audioSignalRms.audioFileName,
            index = AudioUtilsKt.tenthsSecondsFormat(audioSignalRms.index).toFloat(),
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
    val index: Int,
    @Column("channels") val channels: Int,
    @Column("sample_rate") val sampleRate: Int,
    @Column("sample_size_in_bits") val sampleSizeInBits: Int,
    @Column("sample_size") val sampleSize: Int,
    @Column("frame_size") val frameSize: Int,
    @Column("big_endian") val bigEndian: Boolean,
    @Column("encoding") val encoding: String,
    @Column("content") val content: ByteBuffer
) {
    constructor(audioSignal: AudioSignalKt) :
        this (
            audioFileName = audioSignal.audioFileName,
            index = audioSignal.index,
            channels = audioSignal.audioSourceInfo.channels,
            sampleRate = audioSignal.audioSourceInfo.sampleRate,
            sampleSizeInBits = audioSignal.audioSourceInfo.sampleSizeBits,
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
        result = 31 * result + index
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
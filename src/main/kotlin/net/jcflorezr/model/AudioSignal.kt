package net.jcflorezr.model

import net.jcflorezr.broker.Message
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.nio.ByteBuffer

// TODO: rename it when its java equivalent is removed
data class AudioSignalRmsInfoKt(
    val entityName: String = "audioSignalRms",
    val audioFileName: String,
    val index: Double,
    val rms: Double,
    val samplingRate: Int,
    val audioLength: Int,
    val initialPosition: Int,
    val initialPositionInSeconds: Float,
    val segmentSize: Int,
    val segmentSizeInSeconds: Float,
    val silence: Boolean,
    val active: Boolean
) : Message

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
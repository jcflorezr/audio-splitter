package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegmentBytes
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegmentRms
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.nio.ByteBuffer

@Table(value = "audio_segment")
data class AudioSegmentCassandraRecord(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val sourceAudioFileName: String,
    @PrimaryKeyColumn(name = "segment_start_in_seconds", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val segmentStartInSeconds: Float,
    @Column("segment_end_in_seconds") val segmentEndInSeconds: Float,
    @Column("segment_start") val segmentStart: Int,
    @Column("segment_end") val segmentEnd: Int,
    @Column("audio_segment_rms") val audioSegmentRms: Double,
    @Column("audio_segment_bytes") val audioSegmentBytes: ByteBuffer
) {
    companion object {
        const val TABLE_NAME = "audio_segment"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val SEGMENT_START_IN_SECONDS_COLUMN = "segment_start_in_seconds"

        fun fromEntity(audioSegment: AudioSegment) = audioSegment.run {
            AudioSegmentCassandraRecord(
                sourceAudioFileName, segmentStartInSeconds, segmentEndInSeconds, segmentStart,
                segmentEnd, audioSegmentRms.rms, ByteBuffer.wrap(audioSegmentBytes.bytes))
        }
    }

    fun translate() =
        AudioSegment(
            sourceAudioFileName, segmentStart, segmentStartInSeconds, segmentEnd, segmentEndInSeconds,
            AudioSegmentRms(audioSegmentRms), AudioSegmentBytes(audioSegmentBytes.array()))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioSegmentCassandraRecord

        if (sourceAudioFileName != other.sourceAudioFileName) return false
        if (segmentStartInSeconds != other.segmentStartInSeconds) return false
        if (segmentEndInSeconds != other.segmentEndInSeconds) return false
        if (segmentStart != other.segmentStart) return false
        if (segmentEnd != other.segmentEnd) return false
        if (audioSegmentRms != other.audioSegmentRms) return false
        if (audioSegmentBytes != other.audioSegmentBytes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sourceAudioFileName.hashCode()
        result = 31 * result + segmentStartInSeconds.hashCode()
        result = 31 * result + segmentEndInSeconds.hashCode()
        result = 31 * result + segmentStart
        result = 31 * result + segmentEnd
        result = 31 * result + audioSegmentRms.hashCode()
        result = 31 * result + audioSegmentBytes.hashCode()
        return result
    }
}
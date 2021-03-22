package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import com.datastax.driver.core.Row
import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import java.nio.ByteBuffer
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegmentBytes
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegmentRms

@Table(name = AudioSegmentCassandraRecord.TABLE_NAME)
data class AudioSegmentCassandraRecord(
    @PartitionKey(0) @Column(name = AUDIO_FILE_NAME_COLUMN) val sourceAudioFileName: String,
    @ClusteringColumn(0) @Column(name = SEGMENT_START_IN_SECONDS_COLUMN) val segmentStartInSeconds: Float,
    @Column(name = SEGMENT_END_IN_SECONDS_COLUMN) val segmentEndInSeconds: Float,
    @Column(name = SEGMENT_START_COLUMN) val segmentStart: Int,
    @Column(name = SEGMENT_END_COLUMN) val segmentEnd: Int,
    @Column(name = SEGMENT_RMS_COLUMN) val audioSegmentRms: Double,
    @Column(name = SEGMENT_BYTES_COLUMN) val audioSegmentBytes: ByteBuffer
) {
    companion object {
        const val TABLE_NAME = "audio_segment"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val SEGMENT_START_IN_SECONDS_COLUMN = "segment_start_in_seconds"
        const val SEGMENT_END_IN_SECONDS_COLUMN = "segment_end_in_seconds"
        const val SEGMENT_START_COLUMN = "segment_start"
        const val SEGMENT_END_COLUMN = "segment_end"
        const val SEGMENT_RMS_COLUMN = "audio_segment_rms"
        const val SEGMENT_BYTES_COLUMN = "audio_segment_bytes"

        fun fromEntity(audioSegment: AudioSegment) = audioSegment.run {
            AudioSegmentCassandraRecord(
                sourceAudioFileName, segmentStartInSeconds, segmentEndInSeconds, segmentStart,
                segmentEnd, audioSegmentRms.rms, audioSegmentBytes = ByteBuffer.wrap(audioSegmentBytes.bytes)
            )
        }

        fun fromCassandraRow(row: Row) =
            AudioSegmentCassandraRecord(
                sourceAudioFileName = row.getString(AUDIO_FILE_NAME_COLUMN),
                segmentStartInSeconds = row.getFloat(SEGMENT_START_IN_SECONDS_COLUMN),
                segmentEndInSeconds = row.getFloat(SEGMENT_END_IN_SECONDS_COLUMN),
                segmentStart = row.getInt(SEGMENT_START_COLUMN),
                segmentEnd = row.getInt(SEGMENT_END_COLUMN),
                audioSegmentRms = row.getDouble(SEGMENT_RMS_COLUMN),
                audioSegmentBytes = row.getBytes(SEGMENT_BYTES_COLUMN)
            )
    }

    fun translate() =
        AudioSegment(
            sourceAudioFileName, segmentStart, segmentStartInSeconds, segmentEnd, segmentEndInSeconds,
            AudioSegmentRms(audioSegmentRms), AudioSegmentBytes(audioSegmentBytes.array())
        )

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

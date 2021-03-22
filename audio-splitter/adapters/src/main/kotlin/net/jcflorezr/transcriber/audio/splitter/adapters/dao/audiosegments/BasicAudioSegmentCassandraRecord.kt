package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import com.datastax.driver.core.Row
import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment

@Table(name = "audio_segment")
data class BasicAudioSegmentCassandraRecord(
    @PartitionKey(0) @Column(name = "audio_file_name") val sourceAudioFileName: String,
    @ClusteringColumn(0) @Column(name = "segment_start_in_seconds") val segmentStartInSeconds: Float,
    @Column(name = "segment_end_in_seconds") val segmentEndInSeconds: Float,
    @Column(name = "segment_start") val segmentStart: Int,
    @Column(name = "segment_end") val segmentEnd: Int,
    @Column(name = "audio_segment_rms") val audioSegmentRms: Double
) {
    companion object {
        const val TABLE_NAME = "audio_segment"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val SEGMENT_START_IN_SECONDS_COLUMN = "segment_start_in_seconds"
        const val SEGMENT_END_IN_SECONDS_COLUMN = "segment_end_in_seconds"
        const val SEGMENT_START_COLUMN = "segment_start"
        const val SEGMENT_END_COLUMN = "segment_end"
        const val AUDIO_SEGMENT_RMS_COLUMN = "audio_segment_rms"

        fun fromEntity(audioSegment: BasicAudioSegment) = audioSegment.run {
            BasicAudioSegmentCassandraRecord(
                sourceAudioFileName, segmentStartInSeconds, segmentEndInSeconds,
                segmentStart, segmentEnd, audioSegmentRms
            )
        }

        fun fromCassandraRow(row: Row) =
            BasicAudioSegmentCassandraRecord(
                sourceAudioFileName = row.getString(AudioSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN),
                segmentStartInSeconds = row.getFloat(AudioSegmentCassandraRecord.SEGMENT_START_IN_SECONDS_COLUMN),
                segmentEndInSeconds = row.getFloat(AudioSegmentCassandraRecord.SEGMENT_END_IN_SECONDS_COLUMN),
                segmentStart = row.getInt(AudioSegmentCassandraRecord.SEGMENT_START_COLUMN),
                segmentEnd = row.getInt(AudioSegmentCassandraRecord.SEGMENT_END_COLUMN),
                audioSegmentRms = row.getDouble(AudioSegmentCassandraRecord.SEGMENT_RMS_COLUMN)
            )
    }

    fun translate() =
        BasicAudioSegment(
            sourceAudioFileName, segmentStart, segmentStartInSeconds,
            segmentEnd, segmentEndInSeconds, audioSegmentRms
        )
}

package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

@Table(value = "audio_segment")
data class BasicAudioSegmentCassandraRecord(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val sourceAudioFileName: String,
    @PrimaryKeyColumn(name = "segment_start_in_seconds", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val segmentStartInSeconds: Float,
    @Column("segment_end_in_seconds") val segmentEndInSeconds: Float,
    @Column("segment_start") val segmentStart: Int,
    @Column("segment_end") val segmentEnd: Int,
    @Column("audio_segment_rms") val audioSegmentRms: Double
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
                segmentStart, segmentEnd, audioSegmentRms)
        }
    }

    fun translate() =
        BasicAudioSegment(sourceAudioFileName, segmentStart, segmentStartInSeconds,
            segmentEnd, segmentEndInSeconds, audioSegmentRms)
}
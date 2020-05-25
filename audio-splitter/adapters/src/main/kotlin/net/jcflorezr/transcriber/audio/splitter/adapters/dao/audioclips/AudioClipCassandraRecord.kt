package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips

import com.datastax.driver.core.Row
import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.ActiveSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip

data class AudioClipInfoCassandraRecord(
    val audioClipCassandraRecord: AudioClipCassandraRecord,
    val activeSegmentsCassandraRecords: List<ActiveSegmentCassandraRecord>
) {
    companion object {

        fun fromEntity(audioClip: AudioClip) = AudioClipCassandraRecord.fromEntity(audioClip).let { audioClipRecord ->
            AudioClipInfoCassandraRecord(
                audioClipCassandraRecord = audioClipRecord,
                activeSegmentsCassandraRecords = audioClip.activeSegments
                    .map { ActiveSegmentCassandraRecord.fromEntity(audioClipRecord, it) })
        }
    }

    fun translate() = audioClipCassandraRecord
        .translate(activeSegments = activeSegmentsCassandraRecords.map { it.translate() }.toList())
}

@Table(name = AudioClipCassandraRecord.TABLE_NAME)
data class AudioClipCassandraRecord(
    @PartitionKey(0) @Column(name = AUDIO_FILE_NAME_COLUMN) val audioFileName: String,
    @ClusteringColumn(0) @Column(name = HOURS_COLUMN) val hours: Int,
    @ClusteringColumn(1) @Column(name = MINUTES_COLUMN) val minutes: Int,
    @ClusteringColumn(2) @Column(name = SECONDS_COLUMN) val seconds: Int,
    @ClusteringColumn(3) @Column(name = TENTHS_COLUMN) val tenthsOfSecond: Int,
    @Column(name = DURATION_COLUMN) val duration: Float,
    @Column(name = CLIP_FILE_NAME_COLUMN) val clipFileName: String
) {
    companion object {
        const val TABLE_NAME = "audio_clip_info"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val HOURS_COLUMN = "hours"
        const val MINUTES_COLUMN = "minutes"
        const val SECONDS_COLUMN = "seconds"
        const val TENTHS_COLUMN = "tenths_of_second"
        const val DURATION_COLUMN = "duration"
        const val CLIP_FILE_NAME_COLUMN = "clip_file_name"

        fun fromEntity(audioClip: AudioClip) = audioClip.run {
            AudioClipCassandraRecord(
                sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond, duration, audioClipFileName)
        }

        fun fromCassandraRow(row: Row) =
            AudioClipCassandraRecord(
                audioFileName = row.getString(AUDIO_FILE_NAME_COLUMN),
                hours = row.getInt(HOURS_COLUMN),
                minutes = row.getInt(MINUTES_COLUMN),
                seconds = row.getInt(SECONDS_COLUMN),
                tenthsOfSecond = row.getInt(TENTHS_COLUMN),
                duration = row.getFloat(DURATION_COLUMN),
                clipFileName = row.getString(CLIP_FILE_NAME_COLUMN)
            )
    }

    fun translate(activeSegments: List<ActiveSegment>) =
        AudioClip(audioFileName, duration, hours, minutes, seconds, tenthsOfSecond, clipFileName, activeSegments)
}

@Table(name = ActiveSegmentCassandraRecord.TABLE_NAME)
data class ActiveSegmentCassandraRecord(
    @PartitionKey(0) @Column(name = AUDIO_FILE_NAME_COLUMN) val audioFileName: String,
    @ClusteringColumn(0) @Column(name = HOURS_COLUMN) val hours: Int,
    @ClusteringColumn(1) @Column(name = MINUTES_COLUMN) val minutes: Int,
    @ClusteringColumn(2) @Column(name = SECONDS_COLUMN) val seconds: Int,
    @ClusteringColumn(3) @Column(name = TENTHS_COLUMN) val tenthsOfSecond: Int,
    @ClusteringColumn(4) @Column(name = SEGMENT_START_IN_SECONDS_COLUMN) val segmentStartInSeconds: Float,
    @Column(name = SEGMENT_END_IN_SECONDS_COLUMN) val segmentEndInSeconds: Float,
    @Column(name = DURATION_COLUMN) val duration: Float,
    @Column(name = SEGMENT_HOURS_COLUMN) val segmentHours: Int,
    @Column(name = SEGMENT_MINUTES_COLUMN) val segmentMinutes: Int,
    @Column(name = SEGMENT_SECONDS_COLUMN) val segmentSeconds: Int,
    @Column(name = SEGMENT_TENTHS_COLUMN) val segmentTenthsOfSecond: Int
) {
    companion object {
        const val TABLE_NAME = "active_segment"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val HOURS_COLUMN = "hours"
        const val MINUTES_COLUMN = "minutes"
        const val SECONDS_COLUMN = "seconds"
        const val TENTHS_COLUMN = "tenths_of_second"
        const val SEGMENT_START_IN_SECONDS_COLUMN = "segment_start_in_seconds"
        const val SEGMENT_END_IN_SECONDS_COLUMN = "segment_end_in_seconds"
        const val DURATION_COLUMN = "duration"
        const val SEGMENT_HOURS_COLUMN = "segment_hours"
        const val SEGMENT_MINUTES_COLUMN = "segment_minutes"
        const val SEGMENT_SECONDS_COLUMN = "segment_seconds"
        const val SEGMENT_TENTHS_COLUMN = "segment_tenths_of_second"

        fun fromEntity(audioClipRecord: AudioClipCassandraRecord, activeSegment: ActiveSegment) =
            ActiveSegmentCassandraRecord(
                audioClipRecord.audioFileName,
                audioClipRecord.hours,
                audioClipRecord.minutes,
                audioClipRecord.seconds,
                audioClipRecord.tenthsOfSecond,
                activeSegment.segmentStartInSeconds,
                activeSegment.segmentEndInSeconds,
                activeSegment.duration,
                activeSegment.hours,
                activeSegment.minutes,
                activeSegment.seconds,
                activeSegment.tenthsOfSecond)

        fun fromCassandraRow(row: Row) =
            ActiveSegmentCassandraRecord(
                audioFileName = row.getString(AUDIO_FILE_NAME_COLUMN),
                hours = row.getInt(HOURS_COLUMN),
                minutes = row.getInt(MINUTES_COLUMN),
                seconds = row.getInt(SECONDS_COLUMN),
                tenthsOfSecond = row.getInt(TENTHS_COLUMN),
                segmentStartInSeconds = row.getFloat(SEGMENT_START_IN_SECONDS_COLUMN),
                segmentEndInSeconds = row.getFloat(SEGMENT_END_IN_SECONDS_COLUMN),
                duration = row.getFloat(DURATION_COLUMN),
                segmentHours = row.getInt(SEGMENT_HOURS_COLUMN),
                segmentMinutes = row.getInt(SEGMENT_MINUTES_COLUMN),
                segmentSeconds = row.getInt(SEGMENT_SECONDS_COLUMN),
                segmentTenthsOfSecond = row.getInt(SEGMENT_TENTHS_COLUMN)
            )
    }

    fun translate() = ActiveSegment(
        audioFileName, segmentStartInSeconds, segmentEndInSeconds, duration,
        segmentHours, segmentMinutes, segmentSeconds, segmentTenthsOfSecond)
}

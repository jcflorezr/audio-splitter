package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.ActiveSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

data class AudioClipInfoCassandraRecord(
    val audioClipCassandraRecord: AudioClipCassandraRecord,
    val activeSegmentsCassandraRecords: Sequence<ActiveSegmentCassandraRecord>
) {
    companion object {

        fun fromEntity(audioClip: AudioClip) = AudioClipCassandraRecord.fromEntity(audioClip).let { audioClipRecord ->
            AudioClipInfoCassandraRecord(
                audioClipCassandraRecord = audioClipRecord,
                activeSegmentsCassandraRecords = audioClip.activeSegments.asSequence()
                    .map { ActiveSegmentCassandraRecord.fromEntity(audioClipRecord.primaryKey, it) })
        }
    }

    fun translate() = audioClipCassandraRecord
        .translate(activeSegments = activeSegmentsCassandraRecords.map { it.translate() }.toList())
}


@Table(value = "audio_clip_info")
data class AudioClipCassandraRecord(
    @PrimaryKey val primaryKey: AudioClipPrimaryKey,
    @Column("duration") val duration: Float,
    @Column("clip_file_name") val clipFileName: String
) {
    companion object {
        const val TABLE_NAME = "audio_clip_info"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val HOURS_COLUMN = "hours"
        const val MINUTES_COLUMN = "minutes"
        const val SECONDS_COLUMN = "seconds"
        const val TENTHS_COLUMN = "tenths_of_second"

        fun fromEntity(audioClip: AudioClip) = audioClip.run {
            AudioClipCassandraRecord(
                AudioClipPrimaryKey(sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond), duration, audioClipFileName)
        }
    }

    fun translate(activeSegments: List<ActiveSegment>) = primaryKey.run {
        AudioClip(audioFileName, duration, hours, minutes, seconds, tenthsOfSecond, clipFileName, activeSegments)
    }
}

@Table(value = "active_segment")
data class ActiveSegmentCassandraRecord(
    @PrimaryKey val primaryKey: ActiveSegmentPrimaryKey,
    @Column("segment_end_in_seconds")
    val segmentEndInSeconds: Float,
    @Column("duration")
    val duration: Float,
    @Column("segment_hours")
    val segmentHours: Int,
    @Column("segment_minutes")
    val segmentMinutes: Int,
    @Column("segment_seconds")
    val segmentSeconds: Int,
    @Column("segment_tenths_of_second")
    val segmentTenthsOfSecond: Int
) {
    companion object {
        const val TABLE_NAME = "active_segment"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val HOURS_COLUMN = "hours"
        const val MINUTES_COLUMN = "minutes"
        const val SECONDS_COLUMN = "seconds"
        const val TENTHS_COLUMN = "tenths_of_second"

        fun fromEntity(audioClipPrimaryKey: AudioClipPrimaryKey, activeSegment: ActiveSegment) = activeSegment.run {
            ActiveSegmentCassandraRecord(
                primaryKey = ActiveSegmentPrimaryKey.createNew(audioClipPrimaryKey, activeSegment.segmentStartInSeconds),
                segmentEndInSeconds = segmentEndInSeconds,
                duration = duration,
                segmentHours = hours,
                segmentMinutes = minutes,
                segmentSeconds = seconds,
                segmentTenthsOfSecond = tenthsOfSecond)
        }
    }

    fun translate() = ActiveSegment(
        primaryKey.audioFileName, primaryKey.segmentStartInSeconds, segmentEndInSeconds, duration,
        segmentHours, segmentMinutes, segmentSeconds, segmentTenthsOfSecond)
}

@PrimaryKeyClass
data class AudioClipPrimaryKey(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @PrimaryKeyColumn(name = "hours", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val hours: Int,
    @PrimaryKeyColumn(name = "minutes", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    val minutes: Int,
    @PrimaryKeyColumn(name = "seconds", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    val seconds: Int,
    @PrimaryKeyColumn(name = "tenths_of_second", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    val tenthsOfSecond: Int
)

@PrimaryKeyClass
data class ActiveSegmentPrimaryKey(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @PrimaryKeyColumn(name = "hours", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val hours: Int,
    @PrimaryKeyColumn(name = "minutes", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    val minutes: Int,
    @PrimaryKeyColumn(name = "seconds", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    val seconds: Int,
    @PrimaryKeyColumn(name = "tenths_of_second", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    val tenthsOfSecond: Int,
    @PrimaryKeyColumn(name = "segment_start_in_seconds", ordinal = 5, type = PrimaryKeyType.CLUSTERED)
    val segmentStartInSeconds: Float
) {
    companion object {
        fun createNew(audioClipPrimaryKey: AudioClipPrimaryKey, segmentStartInSeconds: Float) = audioClipPrimaryKey.run {
            ActiveSegmentPrimaryKey(audioFileName, hours, minutes, seconds, tenthsOfSecond, segmentStartInSeconds)
        }
    }
}
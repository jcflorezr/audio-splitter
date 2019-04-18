package net.jcflorezr.model

import net.jcflorezr.broker.Message
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

data class AudioClipInfo(
    val audioFileName: String,
    val entityName: String = "audioClip",
    val consecutive: Int,
    val index: Float,
    val sampleRate: Int,
    val initialPosition: Int,
    val initialPositionInSeconds: Float,
    val endPosition: Int,
    val endPositionInSeconds: Float,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int,
    val audioClipName: String,
    var lastClip: Boolean = false
) : Message

@Table(value = "audio_clip_info")
data class AudioClipInfoEntity(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @PrimaryKeyColumn(name = "hours", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val hours: Int,
    @PrimaryKeyColumn(name = "minutes", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    val minutes: Int,
    @PrimaryKeyColumn(name = "seconds", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    val seconds: Int,
    @PrimaryKeyColumn(name = "tenths", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    val tenthsOfSecond: Int,
    @Column("clip_name") val audioClipName: String,
    @Column("initial_position") val initialPosition: Int,
    @Column("initial_position_in_seconds") val initialPositionInSeconds: Float,
    @Column("end_position") val endPosition: Int,
    @Column("end_position_in_seconds") val endPositionInSeconds: Float
) {
    constructor(audioClipInfo: AudioClipInfo) :
        this (
            audioFileName = audioClipInfo.audioFileName,
            hours = audioClipInfo.hours,
            minutes = audioClipInfo.minutes,
            seconds = audioClipInfo.seconds,
            tenthsOfSecond = audioClipInfo.tenthsOfSecond,
            audioClipName = audioClipInfo.audioClipName,
            initialPosition = audioClipInfo.initialPosition,
            initialPositionInSeconds = audioClipInfo.initialPositionInSeconds,
            endPosition = audioClipInfo.endPosition,
            endPositionInSeconds = audioClipInfo.endPositionInSeconds
        )
}

@Table(value = "grouped_audio_clip_info")
data class GroupedAudioClipInfoEntity(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @PrimaryKeyColumn(name = "first_clip_hours", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val firstClipHours: Int,
    @PrimaryKeyColumn(name = "first_clip_minutes", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    val firstClipMinutes: Int,
    @PrimaryKeyColumn(name = "first_clip_seconds", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    val firstClipSeconds: Int,
    @PrimaryKeyColumn(name = "first_clip_tenths", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    val firstClipTenthsOfSecond: Int,
    @Column("last_clip_hours") val lastClipHours: Int,
    @Column("last_clip_minutes") val lastClipMinutes: Int,
    @Column("last_clip_seconds") val lastClipSeconds: Int,
    @Column("last_clip_tenths") val lastClipTenthsOfSecond: Int
) {
    constructor(firstAudioClipInfo: AudioClipInfo, lastAudioClipInfo: AudioClipInfo) :
        this (
            audioFileName = firstAudioClipInfo.audioFileName,
            firstClipHours = firstAudioClipInfo.hours,
            firstClipMinutes = firstAudioClipInfo.minutes,
            firstClipSeconds = firstAudioClipInfo.seconds,
            firstClipTenthsOfSecond = firstAudioClipInfo.tenthsOfSecond,
            lastClipHours = lastAudioClipInfo.hours,
            lastClipMinutes = lastAudioClipInfo.minutes,
            lastClipSeconds = lastAudioClipInfo.seconds,
            lastClipTenthsOfSecond = lastAudioClipInfo.tenthsOfSecond
        )
}
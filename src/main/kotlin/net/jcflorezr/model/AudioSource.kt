package net.jcflorezr.model

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

data class InitialConfiguration(
        val audioFileLocation: String,
        val audioFileMetadata: AudioFileMetadata? = null,
        val convertedAudioFileLocation: String? = null,
        val audioClipsAsStereo: Boolean = false,
        val audioClipsByGroup: ClipsByGroupConfiguration? = null
)

data class ClipsByGroupConfiguration(
    val audioClipsWithSeparator: Boolean = false
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AudioFileMetadata(
        val audioFileName: String,
        val title: String? = null,
        @get:JsonGetter(PREFIX + "album") val album: String? = null,
        @get:JsonGetter(PREFIX + "artist") val artist: String? = null,
        @get:JsonGetter(PREFIX + "trackNumber") val trackNumber: String? = null,
        @get:JsonGetter(PREFIX + "genre") val genre: String? = null,
        @get:JsonGetter(PREFIX + "logComment") val comments: String? = null,
        @get:JsonGetter(PREFIX + "duration") val duration: String? = null,
        @get:JsonGetter(PREFIX + "audioSampleRate") val sampleRate: String? = null,
        val channels: String? = null,
        val version: String? = null,
        val creator: String? = null,
        @get:JsonGetter("Content-Type") val contentType: String? = null,
        val rawMetadata: List<String>? = null
) {
    companion object {
        private const val PREFIX = "xmpDM:"
    }
}

@Table(value = "audio_file_metadata")
data class AudioFileMetadataEntity(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @Column("title") val title: String?,
    @Column("album") val album: String?,
    @Column("artist") val artist: String?,
    @Column("track_number") val trackNumber: String?,
    @Column("genre") val genre: String?,
    @Column("duration") val duration: String?,
    @Column("sample_rate") val sampleRate: String?,
    @Column("content_type") val contentType: String?,
    @Column("channels") val channels: String?,
    @Column("version") val version: String?,
    @Column("creator") val creator: String?,
    @Column("comments") val comments: String?,
    @Column("raw_metadata") var rawMetadata: List<String>?
)
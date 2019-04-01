package net.jcflorezr.model

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.jcflorezr.broker.Message
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import javax.sound.sampled.AudioFormat

data class InitialConfiguration(
    val audioFileLocation: String,
    val audioFileMetadata: AudioFileMetadata? = null,
    val convertedAudioFileLocation: String? = null,
    val outputDirectory: String
) : Message

enum class AudioFormatEncodings {
    PCM_SIGNED,
    PCM_UNSIGNED,
    PCM_FLOAT,
    ALAW,
    ULAW;

    companion object {
        fun getEncoding(encoding: AudioFormat.Encoding) = valueOf(encoding.toString())
    }
}

data class AudioSourceInfo constructor(
    val channels: Int,
    val sampleRate: Int,
    val sampleSizeInBits: Int,
    val frameSize: Int,
    val sampleSize: Int,
    val bigEndian: Boolean,
    val encoding: AudioFormatEncodings
) {

    companion object {

        fun getAudioInfo(format: AudioFormat, buffer: Array<FloatArray?>? = null): AudioSourceInfo {
            val channels = format.channels
            val sampleRate = Math.round(format.sampleRate)
            val bigEndian = format.isBigEndian
            val sampleBits = format.sampleSizeInBits
            val frameSize = format.frameSize
            val sampleSize = (sampleBits + 7) / 8
            val encoding = AudioFormatEncodings.getEncoding(format.encoding)
            when {
                (buffer?.size ?: channels != channels) -> throw IllegalArgumentException("Number of channels not equal to number of buffers.")
                (sampleBits !in listOf(16, 24, 32)) ->
                    throw UnsupportedOperationException("Audio stream format not supported ($sampleBits bits per sample for floating-point PCM).")
                (sampleSize * channels != frameSize) -> throw AssertionError()
            }
            return AudioSourceInfo(channels, sampleRate, sampleBits, frameSize, sampleSize, bigEndian, encoding)
        }
    }
}

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
package net.jcflorezr.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import net.jcflorezr.broker.Message
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.id3.ID3v1Tag
import org.jaudiotagger.tag.id3.ID3v24Frames
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.io.File
import javax.sound.sampled.AudioFormat

data class InitialConfiguration(
    val audioFileName: String,
    val audioFileMetadata: AudioFileMetadata? = null,
    val convertedAudioFileLocation: String? = null
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
    val album: String? = null,
    val artist: String? = null,
    val trackNumber: String? = null,
    val genre: String? = null,
    val comments: String? = null,
    val duration: Int? = null,
    val sampleRate: String? = null,
    val channels: String? = null
) {
    companion object {
        fun getAudioFileMetadata(audioFile: File): AudioFileMetadata {
            val audioFileIO = AudioFileIO.read(audioFile)
            return when (audioFile) {
                is MP3File -> extractMetadataForMp3Files(audioFile)
                else -> {
                    val audioFileTags = audioFileIO.tag
                    val audioFileHeaders = audioFileIO.audioHeader
                    AudioFileMetadata(
                        audioFileName = audioFile.name,
                        title = audioFileTags.getFirst(FieldKey.TITLE),
                        album = audioFileTags.getFirst(FieldKey.ALBUM),
                        artist = audioFileTags.getFirst(FieldKey.ARTIST),
                        trackNumber = audioFileTags.getFirst(FieldKey.TRACK) ?: audioFileTags.getFirst("TRACKNUMBER"),
                        genre = audioFileTags.getFirst(FieldKey.GENRE),
                        comments = audioFileTags.getFirst(FieldKey.COMMENT).takeIf { it.isNotBlank() } ?: audioFileTags.getFirst("COMMENTS"),
                        duration = audioFileHeaders.trackLength,
                        sampleRate = audioFileHeaders.sampleRate,
                        channels = audioFileHeaders.channels
                    )
                }
            }
        }

        private fun extractMetadataForMp3Files(audioFile: MP3File): AudioFileMetadata {
            val tags = audioFile.tag
            return when (tags) {
                is ID3v1Tag -> AudioFileMetadata(
                    audioFileName = audioFile.file.name,
                    title = tags.firstTitle,
                    album = tags.firstAlbum,
                    artist = tags.firstArtist,
                    trackNumber = tags.firstTrack,
                    genre = tags.firstGenre,
                    comments = tags.firstComment
                )
                else -> {
                    val headers = audioFile.mP3AudioHeader
                    AudioFileMetadata(
                        audioFileName = audioFile.file.name,
                        title = tags.getFirst(ID3v24Frames.FRAME_ID_TITLE),
                        album = tags.getFirst(ID3v24Frames.FRAME_ID_ALBUM),
                        artist = tags.getFirst(ID3v24Frames.FRAME_ID_ARTIST),
                        trackNumber = tags.getFirst(ID3v24Frames.FRAME_ID_TRACK),
                        genre = tags.getFirst(ID3v24Frames.FRAME_ID_GENRE),
                        comments = tags.getFirst(ID3v24Frames.FRAME_ID_COMMENT),
                        duration = headers.trackLength,
                        sampleRate = headers.sampleRate,
                        channels = headers.channels
                    )
                }
            }
        }
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
    @Column("duration") val duration: Int?,
    @Column("sample_rate") val sampleRate: String?,
    @Column("channels") val channels: String?,
    @Column("comments") val comments: String?
)
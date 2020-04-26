package net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioFormatEncodings
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileMetadata
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

data class SourceFileInfoCassandraRecord(
    val sourceFileMetadataCassandraRecord: SourceFileMetadataCassandraRecord,
    val sourceFileContentInfoCassandraRecord: SourceFileContentInfoCassandraRecord
) {
    companion object {

        fun fromEntity(audioSourceFileInfo: AudioSourceFileInfo) =
            SourceFileInfoCassandraRecord(
                sourceFileMetadataCassandraRecord =
                    SourceFileMetadataCassandraRecord.fromEntity(audioSourceFileInfo),
                sourceFileContentInfoCassandraRecord =
                    SourceFileContentInfoCassandraRecord.fromEntity(audioSourceFileInfo))
    }

    fun translate() =
        AudioSourceFileInfo(
            originalAudioFile = sourceFileMetadataCassandraRecord.audioFileName,
            audioContentInfo = sourceFileContentInfoCassandraRecord.translate(),
            metadata = sourceFileMetadataCassandraRecord.translate())
}

@Table(value = "source_file_metadata")
data class SourceFileMetadataCassandraRecord(
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
) {
    companion object {
        const val TABLE_NAME = "source_file_metadata"
        const val PRIMARY_COLUMN_NAME = "audio_file_name"

        internal fun fromEntity(audioSourceFileInfo: AudioSourceFileInfo) = audioSourceFileInfo.metadata.run {
            SourceFileMetadataCassandraRecord(audioFileName, title, album, artist, trackNumber,
                genre, duration, sampleRate, channels, comments)
        }
    }

    internal fun translate() = AudioSourceFileMetadata(audioFileName, title, album, artist, trackNumber,
        genre, comments, duration, sampleRate, channels)
}

@Table(value = "source_file_content_info")
data class SourceFileContentInfoCassandraRecord(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @Column("channels") val channels: Int,
    @Column("sample_rate") val sampleRate: Int,
    @Column("sample_size_in_bits") val sampleSizeInBits: Int,
    @Column("frame_size") val frameSize: Int,
    @Column("sample_size") val sampleSize: Int,
    @Column("big_endian") val bigEndian: Boolean,
    @Column("encoding") val encoding: String,
    @Column("total_frames") val totalFrames: Int,
    @Column("exact_total_frames") val exactTotalFrames: Int,
    @Column("total_frames_by_second") val totalFramesBySeconds: Int,
    @Column("remaining_frames") val remainingFrames: Int,
    @Column("frames_per_second") val framesPerSecond: Int,
    @Column("num_of_audio_segments") val numOfAudioSegments: Int,
    @Column("audio_segments_length") val audioSegmentLength: Int,
    @Column("audio_segment_length_in_bytes") val audioSegmentLengthInBytes: Int,
    @Column("audio_segments_per_second") val audioSegmentsPerSecond: Int,
    @Column("remaining_audio_segments") val remainingAudioSegments: Int
) {
    companion object {
        const val TABLE_NAME = "source_file_content_info"
        const val PRIMARY_COLUMN_NAME = "audio_file_name"

        internal fun fromEntity(audioSourceFileInfo: AudioSourceFileInfo) = audioSourceFileInfo.audioContentInfo.run {
            SourceFileContentInfoCassandraRecord(
                audioSourceFileInfo.originalAudioFile, channels, sampleRate, sampleSizeInBits, frameSize, sampleSize, bigEndian,
                encoding.name, totalFrames, exactTotalFrames, totalFramesBySeconds, remainingFrames, framesPerSecond,
                numOfAudioSegments, audioSegmentLength, audioSegmentLengthInBytes, audioSegmentsPerSecond, remainingAudioSegments)
        }
    }

    internal fun translate() =
        AudioContentInfo(
            channels, sampleRate, sampleSizeInBits, frameSize, sampleSize,
            bigEndian, AudioFormatEncodings.getEncoding(encoding),
            totalFrames, exactTotalFrames, totalFramesBySeconds, remainingFrames,
            framesPerSecond, numOfAudioSegments, audioSegmentLength, audioSegmentLengthInBytes,
            audioSegmentsPerSecond, remainingAudioSegments)
}

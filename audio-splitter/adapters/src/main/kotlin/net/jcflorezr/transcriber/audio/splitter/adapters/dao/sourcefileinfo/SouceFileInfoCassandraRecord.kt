package net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo

import com.datastax.driver.core.Row
import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioFormatEncodings
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileMetadata

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
                SourceFileContentInfoCassandraRecord.fromEntity(audioSourceFileInfo)
            )
    }

    fun translate() =
        AudioSourceFileInfo(
            originalAudioFile = sourceFileMetadataCassandraRecord.audioFileName,
            audioContentInfo = sourceFileContentInfoCassandraRecord.translate(),
            metadata = sourceFileMetadataCassandraRecord.translate()
        )
}

@Table(name = SourceFileMetadataCassandraRecord.TABLE_NAME)
data class SourceFileMetadataCassandraRecord(
    @PartitionKey(0) @Column(name = AUDIO_FILE_NAME_COLUMN) val audioFileName: String,
    @Column(name = TITLE_COLUMN) val title: String?,
    @Column(name = ALBUM_COLUMN) val album: String?,
    @Column(name = ARTIST_COLUMN) val artist: String?,
    @Column(name = TRACK_NUMBER_COLUMN) val trackNumber: String?,
    @Column(name = GENRE_COLUMN) val genre: String?,
    @Column(name = DURATION_COLUMN) val duration: Int?,
    @Column(name = SAMPLE_RATE_COLUMN) val sampleRate: String?,
    @Column(name = CHANNELS_COLUMN) val channels: String?,
    @Column(name = COMMENTS_COLUMN) val comments: String?
) {
    companion object {
        const val TABLE_NAME = "source_file_metadata"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val TITLE_COLUMN = "title"
        const val ALBUM_COLUMN = "album"
        const val ARTIST_COLUMN = "artist"
        const val TRACK_NUMBER_COLUMN = "track_number"
        const val GENRE_COLUMN = "genre"
        const val DURATION_COLUMN = "duration"
        const val SAMPLE_RATE_COLUMN = "sample_rate"
        const val CHANNELS_COLUMN = "channels"
        const val COMMENTS_COLUMN = "comments"

        fun fromEntity(audioSourceFileInfo: AudioSourceFileInfo) = audioSourceFileInfo.metadata.run {
            SourceFileMetadataCassandraRecord(
                audioFileName, title, album, artist, trackNumber,
                genre, duration, sampleRate, channels, comments
            )
        }

        fun fromCassandraRow(row: Row) =
            SourceFileMetadataCassandraRecord(
                audioFileName = row.getString(AUDIO_FILE_NAME_COLUMN),
                title = row.getString(TITLE_COLUMN),
                album = row.getString(ALBUM_COLUMN),
                artist = row.getString(ARTIST_COLUMN),
                trackNumber = row.getString(TRACK_NUMBER_COLUMN),
                genre = row.getString(GENRE_COLUMN),
                duration = row.getInt(DURATION_COLUMN),
                sampleRate = row.getString(SAMPLE_RATE_COLUMN),
                channels = row.getString(CHANNELS_COLUMN),
                comments = row.getString(COMMENTS_COLUMN)
            )
    }

    fun translate() = AudioSourceFileMetadata(
        audioFileName, title, album, artist, trackNumber,
        genre, comments, duration, sampleRate, channels
    )
}

@Table(name = SourceFileContentInfoCassandraRecord.TABLE_NAME)
data class SourceFileContentInfoCassandraRecord(
    @PartitionKey(0) @Column(name = AUDIO_FILE_NAME_COLUMN) val audioFileName: String,
    @Column(name = CHANNELS_COLUMN) val channels: Int,
    @Column(name = SAMPLE_RATE_COLUMN) val sampleRate: Int,
    @Column(name = SAMPLE_SIZE_IN_BITS_COLUMN) val sampleSizeInBits: Int,
    @Column(name = FRAME_SIZE_COLUMN) val frameSize: Int,
    @Column(name = SAMPLE_SIZE_COLUMN) val sampleSize: Int,
    @Column(name = BIG_ENDIAN_COLUMN) val bigEndian: Boolean,
    @Column(name = ENCODING_COLUMN) val encoding: String,
    @Column(name = TOTAL_FRAMES_COLUMN) val totalFrames: Int,
    @Column(name = EXACT_TOTAL_FRAMES_COLUMN) val exactTotalFrames: Int,
    @Column(name = TOTAL_FRAMES_BY_SECOND_COLUMN) val totalFramesBySeconds: Int,
    @Column(name = REMAINING_FRAMES_COLUMN) val remainingFrames: Int,
    @Column(name = FRAMES_PER_SECOND_COLUMN) val framesPerSecond: Int,
    @Column(name = NUM_OF_AUDIO_SEGMENTS_COLUMN) val numOfAudioSegments: Int,
    @Column(name = AUDIO_SEGMENT_LENGTH_COLUMN) val audioSegmentLength: Int,
    @Column(name = AUDIO_SEGMENT_LENGTH_IN_BYTES_COLUMN) val audioSegmentLengthInBytes: Int,
    @Column(name = AUDIO_SEGMENTS_PER_SECOND_COLUMN) val audioSegmentsPerSecond: Int,
    @Column(name = REMAINING_AUDIO_SEGMENTS_COLUMN) val remainingAudioSegments: Int
) {
    companion object {
        const val TABLE_NAME = "source_file_content_info"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val CHANNELS_COLUMN = "channels"
        const val SAMPLE_RATE_COLUMN = "sample_rate"
        const val SAMPLE_SIZE_IN_BITS_COLUMN = "sample_size_in_bits"
        const val FRAME_SIZE_COLUMN = "frame_size"
        const val SAMPLE_SIZE_COLUMN = "sample_size"
        const val BIG_ENDIAN_COLUMN = "big_endian"
        const val ENCODING_COLUMN = "encoding"
        const val TOTAL_FRAMES_COLUMN = "total_frames"
        const val EXACT_TOTAL_FRAMES_COLUMN = "exact_total_frames"
        const val TOTAL_FRAMES_BY_SECOND_COLUMN = "total_frames_by_second"
        const val REMAINING_FRAMES_COLUMN = "remaining_frames"
        const val FRAMES_PER_SECOND_COLUMN = "frames_per_second"
        const val NUM_OF_AUDIO_SEGMENTS_COLUMN = "num_of_audio_segments"
        const val AUDIO_SEGMENT_LENGTH_COLUMN = "audio_segment_length"
        const val AUDIO_SEGMENT_LENGTH_IN_BYTES_COLUMN = "audio_segment_length_in_bytes"
        const val AUDIO_SEGMENTS_PER_SECOND_COLUMN = "audio_segments_per_second"
        const val REMAINING_AUDIO_SEGMENTS_COLUMN = "remaining_audio_segments"

        fun fromEntity(audioSourceFileInfo: AudioSourceFileInfo) = audioSourceFileInfo.audioContentInfo.run {
            SourceFileContentInfoCassandraRecord(
                audioSourceFileInfo.originalAudioFile, channels, sampleRate, sampleSizeInBits, frameSize, sampleSize, bigEndian,
                encoding.name, totalFrames, exactTotalFrames, totalFramesBySeconds, remainingFrames, framesPerSecond,
                numOfAudioSegments, audioSegmentLength, audioSegmentLengthInBytes, audioSegmentsPerSecond, remainingAudioSegments
            )
        }

        fun fromCassandraRow(row: Row) =
            SourceFileContentInfoCassandraRecord(
                audioFileName = row.getString(AUDIO_FILE_NAME_COLUMN),
                channels = row.getInt(CHANNELS_COLUMN),
                sampleRate = row.getInt(SAMPLE_RATE_COLUMN),
                sampleSizeInBits = row.getInt(SAMPLE_SIZE_IN_BITS_COLUMN),
                frameSize = row.getInt(FRAME_SIZE_COLUMN),
                sampleSize = row.getInt(SAMPLE_SIZE_COLUMN),
                bigEndian = row.getBool(BIG_ENDIAN_COLUMN),
                encoding = row.getString(ENCODING_COLUMN),
                totalFrames = row.getInt(TOTAL_FRAMES_COLUMN),
                exactTotalFrames = row.getInt(EXACT_TOTAL_FRAMES_COLUMN),
                totalFramesBySeconds = row.getInt(TOTAL_FRAMES_BY_SECOND_COLUMN),
                remainingFrames = row.getInt(REMAINING_FRAMES_COLUMN),
                framesPerSecond = row.getInt(FRAMES_PER_SECOND_COLUMN),
                numOfAudioSegments = row.getInt(NUM_OF_AUDIO_SEGMENTS_COLUMN),
                audioSegmentLength = row.getInt(AUDIO_SEGMENT_LENGTH_COLUMN),
                audioSegmentLengthInBytes = row.getInt(AUDIO_SEGMENT_LENGTH_IN_BYTES_COLUMN),
                audioSegmentsPerSecond = row.getInt(AUDIO_SEGMENTS_PER_SECOND_COLUMN),
                remainingAudioSegments = row.getInt(REMAINING_AUDIO_SEGMENTS_COLUMN)
            )
    }

    fun translate() =
        AudioContentInfo(
            channels, sampleRate, sampleSizeInBits, frameSize, sampleSize,
            bigEndian, AudioFormatEncodings.getEncoding(encoding),
            totalFrames, exactTotalFrames, totalFramesBySeconds, remainingFrames,
            framesPerSecond, numOfAudioSegments, audioSegmentLength, audioSegmentLengthInBytes,
            audioSegmentsPerSecond, remainingAudioSegments
        )
}

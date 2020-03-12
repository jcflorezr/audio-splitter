package net.jcflorezr.transcriber.audio.splitter.adapters.sourcefileinfo

import mu.KotlinLogging
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.AudioFileMetadataGenerator
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileMetadata
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.id3.ID3v1Tag
import org.jaudiotagger.tag.id3.ID3v24Frames
import java.io.File

class JAudioTaggerMetadataGenerator : AudioFileMetadataGenerator {

    private val logger = KotlinLogging.logger { }

    override fun retrieveAudioFileMetadata(audioFile: File): AudioSourceFileMetadata {
        logger.info { "[1][entry-point] Extracting metadata from source audio file: ${audioFile.name}" }
        val audioFileIO = AudioFileIO.read(audioFile)
        return when (audioFile) {
            is MP3File -> extractMetadataForMp3Files(audioFile)
            else -> {
                val audioFileTags = audioFileIO.tag
                val audioFileHeaders = audioFileIO.audioHeader
                AudioSourceFileMetadata.Builder(audioFileName = audioFile.name)
                    .title(audioFileTags.getFirst(FieldKey.TITLE))
                    .album(audioFileTags.getFirst(FieldKey.ALBUM))
                    .artist(audioFileTags.getFirst(FieldKey.ARTIST))
                    .trackNumber(audioFileTags.getFirst(FieldKey.TRACK) ?: audioFileTags.getFirst("TRACKNUMBER"))
                    .genre(audioFileTags.getFirst(FieldKey.GENRE))
                    .comments(audioFileTags.getFirst(FieldKey.COMMENT).takeIf { it.isNotBlank() } ?: audioFileTags.getFirst("COMMENTS"))
                    .duration(audioFileHeaders.trackLength)
                    .sampleRate(audioFileHeaders.sampleRate)
                    .channels(audioFileHeaders.channels)
                    .build()
            }
        }
    }

    private fun extractMetadataForMp3Files(audioFile: MP3File): AudioSourceFileMetadata {
        return when (val tags = audioFile.tag) {
            is ID3v1Tag -> AudioSourceFileMetadata.Builder(audioFileName = audioFile.file.name)
                .title(tags.firstTitle)
                .album(tags.firstAlbum)
                .artist(tags.firstArtist)
                .trackNumber(tags.firstTrack)
                .genre(tags.firstGenre)
                .comments(tags.firstComment)
                .build()
            else -> {
                val headers = audioFile.mP3AudioHeader
                AudioSourceFileMetadata.Builder(audioFileName = audioFile.file.name)
                    .title(tags.getFirst(ID3v24Frames.FRAME_ID_TITLE))
                    .album(tags.getFirst(ID3v24Frames.FRAME_ID_ALBUM))
                    .artist(tags.getFirst(ID3v24Frames.FRAME_ID_ARTIST))
                    .trackNumber(tags.getFirst(ID3v24Frames.FRAME_ID_TRACK))
                    .genre(tags.getFirst(ID3v24Frames.FRAME_ID_GENRE))
                    .comments(tags.getFirst(ID3v24Frames.FRAME_ID_COMMENT))
                    .duration(headers.trackLength)
                    .sampleRate(headers.sampleRate)
                    .channels(headers.channels)
                    .build()
            }
        }
    }
}
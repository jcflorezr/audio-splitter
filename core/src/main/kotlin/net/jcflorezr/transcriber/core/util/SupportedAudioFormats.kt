package net.jcflorezr.transcriber.core.util

import javax.sound.sampled.AudioFileFormat
import net.jcflorezr.transcriber.core.exception.AudioSourceException
import net.sourceforge.javaflacencoder.FLACFileWriter

enum class SupportedAudioFormats(
    val mimeType: String,
    val extension: String,
    val fileType: AudioFileFormat.Type
) {

    WAV("audio/x-wav", "wav", AudioFileFormat.Type.WAVE),
    WAVE("audio/vnd.wave", "wav", AudioFileFormat.Type.WAVE),
    FLAC("audio/x-flac", "flac", FLACFileWriter.FLAC),
    MP3("audio/mpeg", "mp3", AudioFileFormat.Type("MP3", "mp3")),
    MP3_1("audio/x-mpeg-3", "mp3", AudioFileFormat.Type("MP3", "mp3"));

    companion object {
        fun findFileType(mimeType: String): SupportedAudioFormats =
            values()
                .find { it.mimeType == mimeType }
                ?: throw AudioSourceException.audioFormatTypeNotSupported(mimeType, values().map { it.mimeType })

        fun findExtension(extension: String): AudioFileFormat.Type =
            values()
                .find { it.extension == extension }?.fileType
                ?: throw AudioSourceException
                    .audioFileExtensionNotSupported(extension, values().map { it.extension }.distinct())
    }
}

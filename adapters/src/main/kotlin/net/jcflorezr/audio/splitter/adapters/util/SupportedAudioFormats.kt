package net.jcflorezr.audio.splitter.adapters.util

import net.sourceforge.javaflacencoder.FLACFileWriter
import javax.sound.sampled.AudioFileFormat

enum class SupportedAudioFormats(
    val mimeType: String,
    val extension: String,
    val fileType: AudioFileFormat.Type? = null
) {

    WAV("audio/x-wav", "wav", AudioFileFormat.Type.WAVE),
    WAVE("audio/vnd.wave", "wav", AudioFileFormat.Type.WAVE),
    FLAC("audio/x-flac", "flac", FLACFileWriter.FLAC),
    MP3("audio/mpeg", "mp3"),
    MP3_1("audio/x-mpeg-3", "mp3");

    companion object {
        fun getExtension(mimeType: String): SupportedAudioFormats {
            for (supportedAudioFormat in values()) {
                if (supportedAudioFormat.mimeType == mimeType) {
                    return supportedAudioFormat
                }
            }
            throw UnsupportedOperationException("The file type '$mimeType' is not supported.")
        }

        fun getFileType(extension: String): AudioFileFormat.Type? {
            for (supportedAudioFormat in values()) {
                if (supportedAudioFormat.extension == extension) {
                    return supportedAudioFormat.fileType
                }
            }
            throw UnsupportedOperationException("The file extension '$extension' is not supported.")
        }
    }
}

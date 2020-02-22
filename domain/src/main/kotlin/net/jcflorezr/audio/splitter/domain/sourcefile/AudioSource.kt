package net.jcflorezr.audio.splitter.domain.sourcefile

import net.jcflorezr.audio.splitter.domain.exception.AudioFormatException
import net.jcflorezr.audio.splitter.domain.exception.AudioSourceException
import java.io.File
import java.lang.IllegalArgumentException
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.math.roundToInt

data class AudioSourceFileInfo(
    val originalAudioFile: String,
    val audioContentInfo: AudioContentInfo,
    val metadata: AudioSourceFileMetadata? = null,
    val convertedAudioFile: String? = null
)

data class AudioSourceFileMetadata private constructor(
    val audioFileName: String,
    val title: String?,
    val album: String?,
    val artist: String?,
    val trackNumber: String?,
    val genre: String?,
    val comments: String?,
    val duration: Int?,
    val sampleRate: String?,
    val channels: String?
) {
    data class Builder(
        var audioFileName: String,
        var title: String? = null,
        var album: String? = null,
        var artist: String? = null,
        var trackNumber: String? = null,
        var genre: String? = null,
        var comments: String? = null,
        var duration: Int? = null,
        var sampleRate: String? = null,
        var channels: String? = null) {

        fun audioFileName(audioFileName: String) = apply { this.audioFileName = audioFileName }
        fun title(title: String) = apply { this.title = title }
        fun album(album: String) = apply { this.album = album }
        fun artist(artist: String) = apply { this.artist = artist }
        fun trackNumber(trackNumber: String) = apply { this.trackNumber = trackNumber }
        fun genre(genre: String) = apply { this.genre = genre }
        fun comments(comments: String) = apply { this.comments = comments }
        fun duration(duration: Int) = apply { this.duration = duration }
        fun sampleRate(sampleRate: String) = apply { this.sampleRate = sampleRate }
        fun channels(channels: String) = apply { this.channels = channels }

        fun build() = AudioSourceFileMetadata(
            audioFileName, title, album, artist, trackNumber, genre, comments, duration, sampleRate, channels)
    }
}

data class AudioContentInfo private constructor(
    val channels: Int,
    val sampleRate: Int,
    val sampleSizeInBits: Int,
    val frameSize: Int,
    val sampleSize: Int,
    val bigEndian: Boolean,
    val encoding: AudioFormatEncodings,
    val totalFrames: Int
) {
    companion object {

        private val SAMPLE_BITS_SUPPORTED = listOf(16, 24, 32)

        fun create(audioFile: File): AudioContentInfo {
            val stream = AudioSystem.getAudioInputStream(audioFile)
            val format = stream.format
            return AudioContentInfo(
                channels = format.channels,
                sampleRate = format.sampleRate.roundToInt(),
                frameSize = format.frameSize,
                bigEndian = format.isBigEndian,
                encoding = validateFormatEncoding(format),
                sampleSizeInBits = validateSampleSizeInBits(format),
                sampleSize = validateSampleSize(format),
                totalFrames = calculateTotalFrames(stream))
        }

        private fun validateFormatEncoding(format: AudioFormat) =
            try {
                AudioFormatEncodings.getEncoding(format.encoding)
            } catch (ex: IllegalArgumentException) {
                throw AudioFormatException.unsupportedFormatEncoding(format.encoding, AudioFormatEncodings.values())
            }

        private fun validateSampleSizeInBits(format: AudioFormat) =
            format.sampleSizeInBits
                .takeIf { sampleSizeInBits -> sampleSizeInBits in SAMPLE_BITS_SUPPORTED }
                ?: throw AudioSourceException.audioSampleBitsNotSupported(format.sampleSizeInBits, SAMPLE_BITS_SUPPORTED)

        private fun validateSampleSize(format: AudioFormat) =
            ((format.sampleSizeInBits + 7) / 8)
                .takeIf { sampleSize -> sampleSize * format.channels == format.frameSize }
                ?: throw AudioSourceException.incorrectFrameSize(format.channels, format.frameSize)

        private fun calculateTotalFrames(stream: AudioInputStream) =
            stream.frameLength
                .takeIf { it <= Integer.MAX_VALUE }?.toInt()
                ?: throw AudioSourceException.audioSourceTooLong()
    }
}
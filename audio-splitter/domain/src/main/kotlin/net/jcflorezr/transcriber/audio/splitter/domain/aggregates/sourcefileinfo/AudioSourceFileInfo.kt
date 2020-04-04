package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.domain.exception.AudioFormatException
import net.jcflorezr.transcriber.audio.splitter.domain.exception.AudioSourceException
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import java.io.File
import java.lang.IllegalArgumentException
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.math.roundToInt

/*
    Aggregate Root
 */
data class AudioSourceFileInfo(
    val originalAudioFile: String,
    val audioContentInfo: AudioContentInfo,
    val metadata: AudioSourceFileMetadata,
    val convertedAudioFile: String? = null
) : AggregateRoot

/*
    Entity
 */
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

/*
    Entity
 */
data class AudioContentInfo private constructor(
    val channels: Int,
    val sampleRate: Int,
    val sampleSizeInBits: Int,
    val frameSize: Int,
    val sampleSize: Int,
    val bigEndian: Boolean,
    val encoding: AudioFormatEncodings,
    val totalFrames: Int,
    val exactTotalFrames: Int,
    val totalFramesBySeconds: Int,
    val remainingFrames: Int,
    val framesPerSecond: Int,
    val numOfAudioSegments: Int,
    val audioSegmentLength: Int,
    val audioSegmentLengthInBytes: Int,
    val audioSegmentsPerSecond: Int,
    val remainingAudioSegments: Int
) {
    companion object {

        private val SAMPLE_BITS_SUPPORTED = listOf(16, 24, 32)
        private const val AUDIO_SEGMENTS_PER_SECOND = 10

        fun extractFrom(audioFile: File): AudioContentInfo {
            val stream = AudioSystem.getAudioInputStream(audioFile)
            val format = stream.format
            val sampleRate = format.sampleRate.roundToInt()
            val (totalFrames, exactTotalFrames) = stream.calculateTotalFrames()
            val (numOfAudioSegments, remainingAudioSegments) = calculateNumOfAudioSegments(exactTotalFrames, sampleRate)
            val (totalFramesBySeconds, remainingFrames) =
                calculateTotalFramesBySeconds(sampleRate, numOfAudioSegments, remainingAudioSegments)
            return AudioContentInfo(
                channels = format.channels,
                sampleRate = sampleRate,
                frameSize = format.frameSize,
                bigEndian = format.isBigEndian,
                encoding = format.validateFormatEncoding(),
                sampleSizeInBits = format.validateSampleSizeInBits(),
                sampleSize = format.validateSampleSize(),
                totalFrames = totalFrames,
                exactTotalFrames = exactTotalFrames,
                totalFramesBySeconds = totalFramesBySeconds,
                remainingFrames = remainingFrames,
                framesPerSecond = sampleRate,
                numOfAudioSegments = numOfAudioSegments,
                audioSegmentLength = format.calculateAudioSegmentLength(),
                audioSegmentLengthInBytes = format.calculateAudioSegmentLengthInBytes(),
                audioSegmentsPerSecond = calculateAudioSegmentsPerSecond(sampleRate),
                remainingAudioSegments = remainingAudioSegments)
        }

        private fun AudioFormat.validateFormatEncoding() =
            try {
                AudioFormatEncodings.getEncoding(encoding)
            } catch (ex: IllegalArgumentException) {
                throw AudioFormatException.unsupportedFormatEncoding(encoding, AudioFormatEncodings.values())
            }

        private fun AudioFormat.validateSampleSizeInBits() =
            sampleSizeInBits
                .takeIf { sampleSizeInBits -> sampleSizeInBits in SAMPLE_BITS_SUPPORTED }
                ?: throw AudioSourceException.audioSampleBitsNotSupported(sampleSizeInBits, SAMPLE_BITS_SUPPORTED)

        private fun AudioFormat.validateSampleSize() =
            ((sampleSizeInBits + 7) / 8)
                .takeIf { sampleSize -> sampleSize * channels == frameSize }
                ?: throw AudioSourceException.incorrectFrameSize(channels, frameSize)

        private fun AudioInputStream.calculateTotalFrames(): Pair<Int, Int> {
            val totalFrames = frameLength.toInt()
            val framesPerSecond = format.sampleRate.roundToInt()
            return when {
                totalFrames > Integer.MAX_VALUE -> throw AudioSourceException.audioSourceTooLong()
                totalFrames < framesPerSecond -> throw AudioSourceException.audioSourceTooShort()
                else -> totalFrames to calculateExactTotalFrames(totalFrames, framesPerSecond)
            }
        }

        private fun calculateExactTotalFrames(totalFrames: Int, framesPerSecond: Int) =
            totalFrames - ((totalFrames % framesPerSecond) % (framesPerSecond / AUDIO_SEGMENTS_PER_SECOND))

        private fun calculateNumOfAudioSegments(exactTotalFrames: Int, sampleRate: Int) =
            (exactTotalFrames / (sampleRate / AUDIO_SEGMENTS_PER_SECOND))
            .let { it to it % AUDIO_SEGMENTS_PER_SECOND }

        private fun AudioFormat.calculateAudioSegmentLength() = sampleRate.roundToInt() / AUDIO_SEGMENTS_PER_SECOND

        private fun AudioFormat.calculateAudioSegmentLengthInBytes() = calculateAudioSegmentLength() * frameSize

        private fun calculateAudioSegmentsPerSecond(sampleRate: Int) =
            sampleRate / (sampleRate / AUDIO_SEGMENTS_PER_SECOND)

        private fun calculateTotalFramesBySeconds(
            sampleRate: Int,
            numOfAudioSegments: Int,
            remainingAudioSegments: Int
        ) = (numOfAudioSegments / AUDIO_SEGMENTS_PER_SECOND) * sampleRate to
            remainingAudioSegments * (sampleRate / AUDIO_SEGMENTS_PER_SECOND)
    }

    fun calculateCurrentRequiredFrames(framesStart: Int) =
        framesPerSecond.takeIf { framesStart < totalFramesBySeconds } ?: remainingFrames

    fun calculateNextIteration(framesStart: Int, frameBytesRead: Int) =
        when(val framesRead = frameBytesRead / frameSize) {
            framesPerSecond -> framesStart + framesRead
            remainingFrames -> exactTotalFrames
            else -> totalFramesBySeconds
        }

    fun calculateCurrentNumOfAudioSegments(framesStart: Int) =
        audioSegmentsPerSecond.takeIf { framesStart < totalFramesBySeconds } ?: remainingAudioSegments
}
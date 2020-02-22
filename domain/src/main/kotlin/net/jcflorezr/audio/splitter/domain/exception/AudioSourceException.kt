package net.jcflorezr.audio.splitter.domain.exception

import net.jcflorezr.audio.splitter.domain.sourcefile.AudioFormatEncodings
import java.lang.IllegalArgumentException
import javax.sound.sampled.AudioFormat

class AudioSourceException(
    message: String,
    errorCode: String,
    suggestion: String? = null
) : BadRequestException(message = message, errorCode = errorCode, suggestion = suggestion) {
    companion object {

        fun audioSampleBitsNotSupported(sampleBits: Int, sampleBitsList: List<Int>) =
            AudioSourceException(
                errorCode = "audio_sample_bits_not_supported",
                message = "The current audio file contains $sampleBits. Only these sample bits are supported: $sampleBitsList",
                suggestion = "Try with a different audio file")

        fun incorrectFrameSize(channels: Int, frameSize: Int) =
            AudioSourceException(
                errorCode = "audio_has_incorrect_size",
                message = "The current audio file contains $channels channels but it has $frameSize as frame size.",
                suggestion = "Try with a different audio file")

        fun audioSourceTooLong() =
            AudioSourceException(
                errorCode = "audio_source_too_long",
                message = "The current audio file is too long.",
                suggestion = "Try with a shorter audio file")
    }
}

class AudioFormatException(
    errorCode: String,
    exception: Exception
) : InternalServerErrorException(errorCode = errorCode, exception = exception) {
    companion object {

        fun unsupportedFormatEncoding(formatEncoding: AudioFormat.Encoding, supportedFormatEncodingsList: Array<AudioFormatEncodings>) =
            TempLocalFileException(
                errorCode = "audio_format_encoding_not_supported",
                exception = IllegalArgumentException("The audio with format encoding $formatEncoding is not valid to extract the content info. " +
                    "Only audio files with encodings $supportedFormatEncodingsList are valid."))
    }
}

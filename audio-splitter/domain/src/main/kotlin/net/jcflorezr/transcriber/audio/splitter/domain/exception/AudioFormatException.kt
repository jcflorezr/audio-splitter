package net.jcflorezr.transcriber.audio.splitter.domain.exception

import javax.sound.sampled.AudioFormat
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioFormatEncodings
import net.jcflorezr.transcriber.core.exception.InternalServerErrorException

class AudioFormatException(
    errorCode: String,
    exception: Exception
) : InternalServerErrorException(errorCode = errorCode, throwable = exception) {
    companion object {

        fun unsupportedFormatEncoding(formatEncoding: AudioFormat.Encoding, supportedFormatEncodingsList: Array<AudioFormatEncodings>) =
            AudioFormatException(
                errorCode = "audio_format_encoding_not_supported",
                exception = IllegalArgumentException("The audio with format encoding $formatEncoding is not valid to extract the content info. " +
                    "Only audio files with encodings $supportedFormatEncodingsList are valid."))
    }
}

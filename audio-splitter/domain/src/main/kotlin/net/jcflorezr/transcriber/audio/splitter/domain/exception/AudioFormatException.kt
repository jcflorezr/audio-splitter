package net.jcflorezr.transcriber.audio.splitter.domain.exception

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioFormatEncodings
import net.jcflorezr.transcriber.core.exception.InternalServerErrorException
import javax.sound.sampled.AudioFormat

class AudioFormatException(
    errorCode: String,
    exception: Exception
) : InternalServerErrorException(errorCode = errorCode, exception = exception) {
    companion object {

        fun unsupportedFormatEncoding(formatEncoding: AudioFormat.Encoding, supportedFormatEncodingsList: Array<AudioFormatEncodings>) =
            AudioFormatException(
                errorCode = "audio_format_encoding_not_supported",
                exception = IllegalArgumentException("The audio with format encoding $formatEncoding is not valid to extract the content info. " +
                    "Only audio files with encodings $supportedFormatEncodingsList are valid."))
    }
}
package net.jcflorezr.transcriber.core.exception

import java.io.IOException

class AudioSegmentException(
    errorCode: String,
    exception: Exception
) : InternalServerErrorException(errorCode = errorCode, exception = exception) {
    companion object {

        fun unexpectedEndOfFile(requiredFrames: Int, initialPosition: Int, frameSize: Int) =
            AudioSegmentException(
                errorCode = "unexpected_end_of_wav_file",
                exception = IOException("Unexpected End of File while reading WAV file. " +
                    "TotalFrames=$requiredFrames pos=$initialPosition frameSize=$frameSize."))

        fun incorrectSignalLengthForFrameSize(requiredFrames: Int, bytesRead: Int, frameSize: Int) =
            AudioSegmentException(
                errorCode = "incorrect_signal_length_for_frame_size",
                exception = IOException("Length of transmitted signal is not a multiple of frame size. " +
                    "RequiredFrames=$requiredFrames bytesRead=$bytesRead frameSize=$frameSize."))
    }
}

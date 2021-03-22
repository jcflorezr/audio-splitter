package net.jcflorezr.transcriber.core.exception

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
                suggestion = "Try with a different audio file"
            )

        fun incorrectFrameSize(channels: Int, frameSize: Int) =
            AudioSourceException(
                errorCode = "audio_has_incorrect_size",
                message = "The current audio file contains $channels channels but it has $frameSize as frame size.",
                suggestion = "Try with a different audio file"
            )

        fun audioSourceTooLong() =
            AudioSourceException(
                errorCode = "audio_source_too_long",
                message = "The current audio file is too long.",
                suggestion = "Try with a shorter audio file"
            )

        fun audioSourceTooShort() =
            AudioSourceException(
                errorCode = "audio_source_too_short",
                message = "The current audio file is less than a second in duration.",
                suggestion = "Try with a longer audio file"
            )

        fun audioFormatTypeNotSupported(audioType: String, audioTypesSupported: List<String>) =
            AudioSourceException(
                errorCode = "audio_format_not_supported",
                message = "The file format type '$audioType' is not supported.",
                suggestion = "Audio format types supported: $audioTypesSupported"
            )

        fun audioFileExtensionNotSupported(audioFileExtension: String, audioFileExtensionsSupported: List<String>) =
            AudioSourceException(
                errorCode = "audio_file_extension_not_supported",
                message = "The file extension '$audioFileExtension' is not supported.",
                suggestion = "Audio file extensions supported: $audioFileExtensionsSupported"
            )
    }
}

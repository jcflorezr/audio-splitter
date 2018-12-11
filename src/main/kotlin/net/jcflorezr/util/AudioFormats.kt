package net.jcflorezr.util

enum class AudioFormats(
    val mimeType: String,
    val extension: String
) {

    WAV("audio/x-wav", ".wav"),
    WAVE("audio/vnd.wave", ".wav"),
    FLAC("audio/x-flac", ".flac"),
    MP3("audio/mpeg", ".mp3"),
    MP3_1("audio/x-mpeg-3", ".mp3");

    companion object {
        fun getExtension(mimeType: String): AudioFormats {
            for (supportedAudioFormat in AudioFormats.values()) {
                if (supportedAudioFormat.mimeType == mimeType) {
                    return supportedAudioFormat
                }
            }
            throw UnsupportedOperationException("The file type '$mimeType' is not supported.")
        }
    }

}

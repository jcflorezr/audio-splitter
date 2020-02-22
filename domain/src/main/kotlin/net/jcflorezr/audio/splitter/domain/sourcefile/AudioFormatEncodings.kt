package net.jcflorezr.audio.splitter.domain.sourcefile

import javax.sound.sampled.AudioFormat

enum class AudioFormatEncodings {
    PCM_SIGNED,
    PCM_UNSIGNED,
    PCM_FLOAT,
    ALAW,
    ULAW;

    companion object {
        fun getEncoding(encoding: AudioFormat.Encoding) = valueOf(encoding.toString())
    }
}
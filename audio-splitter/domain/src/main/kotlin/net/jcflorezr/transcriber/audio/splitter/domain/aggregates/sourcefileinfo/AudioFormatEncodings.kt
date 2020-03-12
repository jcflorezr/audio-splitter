package net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo

import javax.sound.sampled.AudioFormat

enum class AudioFormatEncodings {
    PCM_SIGNED, PCM_FLOAT;

    companion object {
        fun getEncoding(encoding: AudioFormat.Encoding) = valueOf(encoding.toString())
    }
}
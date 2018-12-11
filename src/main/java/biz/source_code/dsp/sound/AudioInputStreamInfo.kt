package biz.source_code.dsp.sound

import javax.sound.sampled.AudioFormat

internal class AudioInputStreamInfo private constructor(
    val channels: Int,
    val sampleRate: Int,
    val sampleSizeBits: Int,
    val frameSize: Int,
    val sampleSize: Int,
    val isBigEndian: Boolean,
    val encoding: AudioFormat.Encoding
) {

    companion object {

        fun getAudioInfo(format: AudioFormat, buffer: Array<FloatArray?>? = null): AudioInputStreamInfo {

            // TODO: can be done with one channel?
            val channels = 1 //format.channels
            val sampleRate = Math.round(format.sampleRate)
            val bigEndian = format.isBigEndian
            val sampleBits = format.sampleSizeInBits
            val frameSize = format.frameSize
            val sampleSize = (sampleBits + 7) / 8
            val encoding = format.encoding
            when {
                (buffer?.size ?: channels != channels) -> throw IllegalArgumentException("Number of channels not equal to number of buffers.")
                (sampleBits !in listOf(16, 24, 32)) ->
                    throw UnsupportedOperationException("Audio stream format not supported ($sampleBits bits per sample for floating-point PCM).")
                (sampleSize * channels != frameSize) -> throw AssertionError()
            }
            return AudioInputStreamInfo(channels, sampleRate, sampleBits, frameSize, sampleSize, bigEndian, encoding)
        }

    }
}

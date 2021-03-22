package net.jcflorezr.transcriber.audio.splitter.domain.util

import java.io.InputStream
import javax.sound.sampled.AudioFormat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioFormatEncodings

class AudioBytesPacker(
    private val format: AudioFormat,
    private val signal: List<List<Float>>,
    audioInfo: AudioContentInfo
) : InputStream() {

    private var pos: Int = 0
    private val inOffset: Int = 0
    private val inLength: Int = signal[0].size
    private val channels: Int = audioInfo.channels
    private val sampleSize: Int = audioInfo.sampleSize
    private val sampleSizeBits: Int = audioInfo.sampleSizeInBits
    private val frameSize: Int = audioInfo.frameSize
    private val bigEndian: Boolean = audioInfo.bigEndian
    private val encoding: AudioFormatEncodings = audioInfo.encoding

    private val bytesSignedInt16: List<(index: Int) -> Byte>
    private val bytesSignedInt24: List<(index: Int) -> Byte>
    private val bytesUnsignedInt: List<(index: Int) -> Byte>
    private val bytesSignedInt16Reversed: List<(index: Int) -> Byte>
    private val bytesSignedInt24Reversed: List<(index: Int) -> Byte>
    private val bytesUnsignedIntReversed: List<(index: Int) -> Byte>

    init {
        bytesSignedInt16 = listOf(
            { i -> (i and 0xFF).toByte() },
            { i -> (i.ushr(8) and 0xFF).toByte() }
        )
        bytesSignedInt24 = bytesSignedInt16 + listOf { i -> (i.ushr(16) and 0xFF).toByte() }
        bytesUnsignedInt = bytesSignedInt24 + listOf { i -> (i.ushr(24) and 0xFF).toByte() }
        bytesSignedInt16Reversed = bytesSignedInt16.reversed()
        bytesSignedInt24Reversed = bytesSignedInt24.reversed()
        bytesUnsignedIntReversed = bytesUnsignedInt.reversed()
    }

    override fun read(): Int {
        throw AssertionError("Not implemented.")
    }

    override fun read(outBuffer: ByteArray, outOffset: Int, outLength: Int): Int {
        val remainingFrames = inLength - pos
        if (remainingFrames <= 0) {
            return -1
        }
        val requiredFrames = outLength / format.frameSize
        val trFrames = min(remainingFrames, requiredFrames)
        packAudioStreamBytes(signal, inOffset + pos, outBuffer, outOffset, trFrames)
        pos += trFrames
        return trFrames * format.frameSize
    }

    /**
     * A utility routine to pack the signal for a Java Sound audio stream.
     */
    private fun packAudioStreamBytes(
        inBuffer: List<List<Float>>,
        inPosition: Int,
        outBuffer: ByteArray,
        outPosition: Int,
        frames: Int
    ) {
        for (channel in 0 until channels) {
            val p0 = outPosition + channel * sampleSize
            val inBuf = inBuffer[channel]
            for (i in 0 until frames) {
                val bitsAsFloat = max(-1f, min(1f, inBuf[inPosition + i]))
                when (encoding) {
                    AudioFormatEncodings.PCM_SIGNED -> {
                        val maxValue = (1 shl sampleSizeBits - 1) - 1
                        packSignedInt(
                            i = (bitsAsFloat * maxValue).roundToInt(),
                            buffer = outBuffer,
                            position = p0 + i * frameSize,
                            bits = sampleSizeBits,
                            isBigEndian = bigEndian
                        )
                    }
                    AudioFormatEncodings.PCM_FLOAT ->
                        packUnsignedInt(
                            i = java.lang.Float.floatToIntBits(bitsAsFloat),
                            buffer = outBuffer,
                            position = p0 + i * frameSize,
                            isBigEndian = bigEndian
                        )
                }
            }
        }
    }

    private fun packSignedInt(i: Int, buffer: ByteArray, position: Int, bits: Int, isBigEndian: Boolean) {
        when (bits) {
            16 -> when (isBigEndian) {
                true -> bytesSignedInt16Reversed.forEachIndexed { index, func -> buffer[position + index] = func(i) }
                false -> bytesSignedInt16.forEachIndexed { index, func -> buffer[position + index] = func(i) }
            }
            24 -> when (isBigEndian) {
                true -> bytesSignedInt24Reversed.forEachIndexed { index, func -> buffer[position + index] = func(i) }
                false -> bytesSignedInt24.forEachIndexed { index, func -> buffer[position + index] = func(i) }
            }
            32 -> packUnsignedInt(i, buffer, position, isBigEndian)
            else -> throw AssertionError()
        }
    }

    private fun packUnsignedInt(i: Int, buffer: ByteArray, position: Int, isBigEndian: Boolean) {
        when (isBigEndian) {
            true -> bytesUnsignedIntReversed.forEachIndexed { index, func -> buffer[position + index] = func(i) }
            false -> bytesUnsignedInt.forEachIndexed { index, func -> buffer[position + index] = func(i) }
        }
    }
}

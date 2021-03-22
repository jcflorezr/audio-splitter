package net.jcflorezr.transcriber.audio.splitter.domain.util

import java.util.stream.IntStream
import kotlin.streams.asSequence
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioFormatEncodings

/**
 * A utility routine to unpack the signal of a Java Sound audio stream.
 */
object AudioBytesUnPacker {

    fun generateAudioSignal(audioContentInfo: AudioContentInfo, bytesBuffer: ByteArray): List<List<Float>> =
        generateAudioSignal(audioContentInfo, bytesBuffer, 0, bytesBuffer.size / audioContentInfo.frameSize)

    fun generateAudioSignal(audioContentInfo: AudioContentInfo, bytesBuffer: ByteArray, from: Int, to: Int): List<List<Float>> =
        IntStream.range(0, audioContentInfo.channels).asSequence()
            .map { channel -> channel * audioContentInfo.sampleSize }
            .map { p0 -> audioContentInfo.processCurrentChannel(from, to, bytesBuffer, p0) }
            .toList()

    private fun AudioContentInfo.processCurrentChannel(from: Int, to: Int, bytesBuffer: ByteArray, p0: Int): List<Float> =
        IntStream.range(from, to).asSequence()
            .map { i -> generateSignalForFrame(bytesBuffer, p0, i) }
            .toList()

    private fun AudioContentInfo.generateSignalForFrame(bytesBuffer: ByteArray, p0: Int, i: Int): Float =
        when (encoding) {
            AudioFormatEncodings.PCM_SIGNED -> {
                unpackSignedInt(
                    buf = bytesBuffer, pos = p0 + i * frameSize, bits = sampleSizeInBits, bigEndian = bigEndian
                )
                    .div(((1 shl sampleSizeInBits - 1) - 1).toFloat())
            }
            AudioFormatEncodings.PCM_FLOAT -> unpackFloat(bytesBuffer, p0 + i * frameSize, bigEndian)
        }

    private fun unpackSignedInt(buf: ByteArray, pos: Int, bits: Int, bigEndian: Boolean): Int {
        return when (bits) {
            16 -> if (bigEndian) {
                buf[pos].toInt() shl 8 or (buf[pos + 1].toInt() and 0xFF)
            } else {
                buf[pos + 1].toInt() shl 8 or (buf[pos].toInt() and 0xFF)
            }
            24 -> if (bigEndian) {
                buf[pos].toInt() shl 16 or
                    (buf[pos + 1].toInt() and 0xFF shl 8) or
                    (buf[pos + 2].toInt() and 0xFF)
            } else {
                buf[pos + 2].toInt() shl 16 or
                    (buf[pos + 1].toInt() and 0xFF shl 8) or
                    (buf[pos].toInt() and 0xFF)
            }
            32 -> unpackUnsignedInt(buf, pos, bigEndian)
            else -> throw AssertionError()
        }
    }

    private fun unpackUnsignedInt(buf: ByteArray, pos: Int, bigEndian: Boolean): Int {
        return if (bigEndian) {
            buf[pos].toInt() shl 24 or
                (buf[pos + 1].toInt() and 0xFF shl 16) or
                (buf[pos + 2].toInt() and 0xFF shl 8) or
                (buf[pos + 3].toInt() and 0xFF)
        } else {
            buf[pos + 3].toInt() shl 24 or
                (buf[pos + 2].toInt() and 0xFF shl 16) or
                (buf[pos + 1].toInt() and 0xFF shl 8) or
                (buf[pos].toInt() and 0xFF)
        }
    }

    private fun unpackFloat(buf: ByteArray, pos: Int, bigEndian: Boolean): Float {
        val i = unpackUnsignedInt(buf, pos, bigEndian)
        return java.lang.Float.intBitsToFloat(i)
    }
}

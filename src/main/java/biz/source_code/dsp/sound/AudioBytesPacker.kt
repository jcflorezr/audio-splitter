package biz.source_code.dsp.sound

import java.io.InputStream
import javax.sound.sampled.AudioFormat

internal class AudioBytesPacker(
    private val format: AudioFormat,
    private val inBufs: Array<FloatArray?>,
    private val inOffs: Int,
    private val inLen: Int
) : InputStream() {

    private var pos: Int = 0
    private val channels: Int
    private val sampleSize: Int
    private val sampleSizeBits: Int
    private val frameSize: Int
    private val bigEndian: Boolean
    private val encoding: AudioFormat.Encoding

    private val bytesSignedInt16: List<(Int) -> Byte>
    private val bytesSignedInt24: List<(Int) -> Byte>
    private val bytesUnsignedInt: List<(Int) -> Byte>
    private val bytesSignedInt16Reversed: List<(Int) -> Byte>
    private val bytesSignedInt24Reversed: List<(Int) -> Byte>
    private val bytesUnsignedIntReversed: List<(Int) -> Byte>

    init {
        val audioInfo = AudioInputStreamInfo.getAudioInfo(format, inBufs)
        frameSize = audioInfo.frameSize
        sampleSize = audioInfo.sampleSize
        sampleSizeBits = audioInfo.sampleSizeBits
        channels = audioInfo.channels
        bigEndian = audioInfo.isBigEndian
        encoding = audioInfo.encoding
        bytesSignedInt16 = listOf(
            {i -> (i and 0xFF).toByte()},
            {i -> (i.ushr(8) and 0xFF).toByte()})
        bytesSignedInt24 = bytesSignedInt16 + listOf({i -> (i.ushr(16) and 0xFF).toByte()})
        bytesUnsignedInt = bytesSignedInt24 + listOf({i -> (i.ushr(24) and 0xFF).toByte()})
        bytesSignedInt16Reversed = bytesSignedInt16.reversed()
        bytesSignedInt24Reversed = bytesSignedInt24.reversed()
        bytesUnsignedIntReversed = bytesUnsignedInt.reversed()
    }

    override fun read(): Int {
        throw AssertionError("Not implemented.")
    }

    override fun read(outBuf: ByteArray, outOffs: Int, outLen: Int): Int {
        val remFrames = inLen - pos
        if (remFrames <= 0) {
            return -1
        }
        val reqFrames = outLen / format.frameSize
        val trFrames = Math.min(remFrames, reqFrames)
        packAudioStreamBytes(inBufs, inOffs + pos, outBuf, outOffs, trFrames)
        pos += trFrames
        return trFrames * format.frameSize
    }

    /**
     * A utility routine to pack the data for a Java Sound audio stream.
     */
    private fun packAudioStreamBytes(inBufs: Array<FloatArray?>, inPos: Int, outBuf: ByteArray, outPos: Int, frames: Int) {
        val maxValue = (1 shl sampleSizeBits - 1) - 1
        for (channel in 0 until channels) {
            val p0 = outPos + channel * sampleSize
            val inBuf = inBufs[channel]
            for (i in 0 until frames) {
                val clipped = Math.max(-1f, Math.min(1f, inBuf!![inPos + i]))
                when(encoding) {
                    AudioFormat.Encoding.PCM_SIGNED -> {
                        val v = Math.round(clipped * maxValue)
                        packSignedInt(v, outBuf, p0 + i * frameSize, sampleSizeBits, bigEndian)
                    }
                    AudioFormat.Encoding.PCM_FLOAT -> packFloat(clipped, outBuf, p0 + i * frameSize, bigEndian)
                    else -> throw UnsupportedOperationException("Audio stream format not supported (not signed PCM or Float).")
                }
            }
        }
    }

    private fun packSignedInt(i: Int, buf: ByteArray, pos: Int, bits: Int, bigEndian: Boolean) {
        when (bits) {
            16 -> when (bigEndian) {
                true -> bytesSignedInt16Reversed.forEachIndexed { index, func -> buf[pos + index] = func.invoke(i) }
                false -> bytesSignedInt16.forEachIndexed { index, func -> buf[pos + index] = func.invoke(i) }
            }
            24 -> when (bigEndian) {
                true -> bytesSignedInt24Reversed.forEachIndexed { index, func -> buf[pos + index] = func.invoke(i) }
                false -> bytesSignedInt24.forEachIndexed { index, func -> buf[pos + index] = func.invoke(i) }
            }
            32 -> packUnsignedInt(i, buf, pos, bigEndian)
            else -> throw AssertionError()
        }
    }

    private fun packUnsignedInt(i: Int, buf: ByteArray, pos: Int, bigEndian: Boolean) {
        when (bigEndian) {
            true -> bytesUnsignedIntReversed.forEachIndexed { index, func -> buf[pos + index] = func.invoke(i) }
            false -> bytesUnsignedInt.forEachIndexed { index, func -> buf[pos + index] = func.invoke(i) }
        }
    }

    private fun packFloat(f: Float, buf: ByteArray, pos: Int, bigEndian: Boolean) {
        val i = java.lang.Float.floatToIntBits(f)
        packUnsignedInt(i, buf, pos, bigEndian)
    }

}

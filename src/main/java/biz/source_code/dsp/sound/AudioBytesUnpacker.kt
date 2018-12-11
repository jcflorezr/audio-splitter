package biz.source_code.dsp.sound

import java.io.IOException
import javax.sound.sampled.AudioFormat

internal object AudioBytesUnpacker {

    @Throws(IOException::class)
    fun generateAudioSignal(
        audioInfo: AudioInputStreamInfo,
        bytesBuffer: ByteArray,
        framesToRead: Int
    ): Array<FloatArray?> {
//        if (trBytes <= 0) {
//            if (audioInfo.encoding === AudioFormat.Encoding.PCM_FLOAT && pos * audioInfo.frameSize == totalFrames) {
//                // Workaround for JDK bug JDK-8038139 / JI-9011075.
//                // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8038139
//                // https://bugs.openjdk.java.net/browse/JDK-8038139
//                truncateSignal(signal, pos)
//                break
//            }
//            throw IOException("Unexpected EOF while reading WAV file. totalFrames=" + totalFrames + " pos=" + pos + " frameSize=" + audioInfo.frameSize + ".")
//        }
//        if (trBytes % audioInfo.frameSize != 0) {
//            throw IOException("Length of transmitted data is not a multiple of frame size. reqFrames=" + reqFrames + " trBytes=" + trBytes + " frameSize=" + audioInfo.frameSize + ".")
//        }
        return unpackAudioStreamBytes(audioInfo, bytesBuffer, framesToRead)
    }

//    private fun truncateSignal(signal: AudioSignal, length: Int) {
//        for (channel in 0 until signal.channels) {
//            signal.data[channel] = Arrays.copyOf(signal.data[channel], length)
//        }
//    }

    /**
     * A utility routine to unpack the data of a Java Sound audio stream.
     */
    private fun unpackAudioStreamBytes(
        audioInfo: AudioInputStreamInfo,
        bytesBuffer: ByteArray,
        frames: Int
    ): Array<FloatArray?> {
        val signal = arrayOfNulls<FloatArray>(audioInfo.channels)
        for (channel in 0 until audioInfo.channels) {
            signal[channel] = FloatArray(frames)
        }
        val encoding = audioInfo.encoding
        val maxValue = ((1 shl audioInfo.sampleSizeBits - 1) - 1).toFloat()
        for (channel in 0 until audioInfo.channels) {
            val p0 = channel * audioInfo.sampleSize
            val outBuff = signal[channel]
            for (i in 0 until frames) {
                when (encoding) {
                    AudioFormat.Encoding.PCM_SIGNED -> {
                        val v = unpackSignedInt(bytesBuffer, p0 + i * audioInfo.frameSize, audioInfo.sampleSizeBits, audioInfo.isBigEndian)
                        outBuff!![i] = v / maxValue
                    }
                    AudioFormat.Encoding.PCM_FLOAT -> outBuff!![i] = unpackFloat(bytesBuffer, p0 + i * audioInfo.frameSize, audioInfo.isBigEndian)
                    else -> throw UnsupportedOperationException("Audio stream format not supported (not signed PCM or Float).")
                }
            }
        }
        return signal
    }

    private fun unpackSignedInt(buf: ByteArray, pos: Int, bits: Int, bigEndian: Boolean): Int {
        return when (bits) {
            16 -> if (bigEndian) {
                buf[pos].toInt() shl 8 or (buf[pos + 1].toInt() and 0xFF)
            } else {
                buf[pos + 1].toInt() shl 8 or (buf[pos].toInt() and 0xFF)
            }
            24 -> if (bigEndian) {
                buf[pos].toInt() shl 16 or (buf[pos + 1].toInt() and 0xFF shl 8) or (buf[pos + 2].toInt() and 0xFF)
            } else {
                buf[pos + 2].toInt() shl 16 or (buf[pos + 1].toInt() and 0xFF shl 8) or (buf[pos].toInt() and 0xFF)
            }
            32 -> unpackUnsignedInt(buf, pos, bigEndian)
            else -> throw AssertionError()
        }
    }

    private fun unpackUnsignedInt(buf: ByteArray, pos: Int, bigEndian: Boolean): Int {
        return if (bigEndian) {
            buf[pos].toInt() shl 24 or (buf[pos + 1].toInt() and 0xFF shl 16) or (buf[pos + 2].toInt() and 0xFF shl 8) or (buf[pos + 3].toInt() and 0xFF)
        } else {
            buf[pos + 3].toInt() shl 24 or (buf[pos + 2].toInt() and 0xFF shl 16) or (buf[pos + 1].toInt() and 0xFF shl 8) or (buf[pos].toInt() and 0xFF)
        }
    }

    private fun unpackFloat(buf: ByteArray, pos: Int, bigEndian: Boolean): Float {
        val i = unpackUnsignedInt(buf, pos, bigEndian)
        return java.lang.Float.intBitsToFloat(i)
    }

}

package biz.source_code.dsp.model

import net.jcflorezr.broker.Message
import net.jcflorezr.model.AudioSourceInfo

// TODO: rename it once its equivalent in java is removed
data class AudioSignalKt(
    val entityName: String = "audioSignal",
    val audioFileName: String,
    val index: Float,
    val sampleRate: Int,
    val totalFrames: Int,
    val initialPosition: Int,
    val initialPositionInSeconds: Float,
    val endPosition: Int,
    val endPositionInSeconds: Float,
    val data: Array<FloatArray?>,
    val dataInBytes: ByteArray,
    val audioSourceInfo: AudioSourceInfo
) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioSignalKt

        if (entityName != other.entityName) return false
        if (audioFileName != other.audioFileName) return false
        if (index != other.index) return false
        if (sampleRate != other.sampleRate) return false
        if (totalFrames != other.totalFrames) return false
        if (initialPosition != other.initialPosition) return false
        if (initialPositionInSeconds != other.initialPositionInSeconds) return false
        if (endPosition != other.endPosition) return false
        if (endPositionInSeconds != other.endPositionInSeconds) return false
        if (!data.contentDeepEquals(other.data)) return false
        if (!dataInBytes.contentEquals(other.dataInBytes)) return false
        if (audioSourceInfo != other.audioSourceInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = entityName.hashCode()
        result = 31 * result + audioFileName.hashCode()
        result = 31 * result + index.hashCode()
        result = 31 * result + sampleRate
        result = 31 * result + totalFrames
        result = 31 * result + initialPosition
        result = 31 * result + initialPositionInSeconds.hashCode()
        result = 31 * result + endPosition
        result = 31 * result + endPositionInSeconds.hashCode()
        result = 31 * result + data.contentDeepHashCode()
        result = 31 * result + dataInBytes.contentHashCode()
        result = 31 * result + audioSourceInfo.hashCode()
        return result
    }
}

data class AudioClipSignal(
    val sampleRate: Int,
    val signal: Array<FloatArray?>,
    val audioClipName: String,
    val audioFileName: String
) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioClipSignal

        if (sampleRate != other.sampleRate) return false
        if (!signal.contentDeepEquals(other.signal)) return false
        if (audioClipName != other.audioClipName) return false
        if (audioFileName != other.audioFileName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sampleRate
        result = 31 * result + signal.contentDeepHashCode()
        result = 31 * result + audioClipName.hashCode()
        result = 31 * result + audioFileName.hashCode()
        return result
    }
}

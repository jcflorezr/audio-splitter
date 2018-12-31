package biz.source_code.dsp.model

import net.jcflorezr.broker.Message
import net.jcflorezr.model.AudioSourceInfo

// TODO: rename it once its equivalent in java is removed
data class AudioSignalKt(
    val entityName: String = "audioSignal",
    val audioFileName: String,
    val index: Int,
    val samplingRate: Int,
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
        if (samplingRate != other.samplingRate) return false
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
        result = 31 * result + index
        result = 31 * result + samplingRate
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

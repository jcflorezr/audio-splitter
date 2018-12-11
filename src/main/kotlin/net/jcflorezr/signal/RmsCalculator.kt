package net.jcflorezr.signal

import biz.source_code.dsp.model.AudioSignalKt
import net.jcflorezr.broker.Topic
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.util.AudioUtilsKt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface RmsCalculator {
    fun generateRmsInfo(audioSignal: AudioSignalKt)
}

@Service
class RmsCalculatorImpl : RmsCalculator {

    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioSignalRmsInfoKt>

    companion object {
        private const val SILENCE_THRESHOLD = 0.001
        private const val ACTIVE_THRESHOLD = 0.03
    }

    override fun generateRmsInfo(audioSignal: AudioSignalKt) {
        val samplingRate = audioSignal.samplingRate
        val segmentSize = samplingRate / 10 // 0.1 secs
        val signal = audioSignal.data[0]!! // always mono
        generateSequence(RmsValues(pos = 0, index = 1, rms = 0.0, diff = 0.0)) {
            // If the last segment is less than 2/3 of the segment size, we include it in the previous segment.
            val endPos = if (it.pos + segmentSize * 5 / 3 > signal.size) signal.size else it.pos + segmentSize
            val currentRms = AudioUtilsKt
                .millisecondsFormat(
                    value = computeRms(
                        signal = signal,
                        startPosition = it.pos,
                        length = endPos - it.pos
                    )
                )
            val positionInSeconds = it.pos.toFloat() / samplingRate
            val currentDiff = AudioUtilsKt.millisecondsFormat(value = (it.rms - currentRms))
            val deepDiff = AudioUtilsKt.millisecondsFormat(value = (it.diff - currentDiff))
            val silence = Math.abs(currentDiff) <= SILENCE_THRESHOLD
            val active = Math.abs(deepDiff) >= ACTIVE_THRESHOLD
            val initialPosInSeconds = positionInSeconds + audioSignal.initialPositionInSeconds
            val rmsSignalInfo = AudioSignalRmsInfoKt(
                audioFileName = audioSignal.audioFileName,
                index = AudioUtilsKt.tenthsSecondsFormat(initialPosInSeconds.toDouble()),
                rms = currentRms,
                audioLength = audioSignal.totalFrames,
                samplingRate = samplingRate,
                initialPosition = it.pos + audioSignal.initialPosition,
                initialPositionInSeconds = initialPosInSeconds,
                segmentSize = segmentSize,
                segmentSizeInSeconds = AudioUtilsKt.tenthsSecondsFormat(segmentSize / samplingRate.toDouble()).toFloat(),
                silence = silence,
                active = active
            )
            // TODO: Thread is not the option
            Thread { audioSignalRmsTopic.postMessage(msg = rmsSignalInfo) }.start()
            RmsValues(pos = endPos, index = it.index + 1, rms = currentRms, diff = currentDiff)
        }.takeWhile { it.pos < signal.size }
        .toList()
        // TODO: should return a result
    }

    private fun computeRms(signal: FloatArray, startPosition: Int, length: Int): Double {
        var a = 0.0
        for (p in startPosition until startPosition + length) {
            a += (signal[p] * signal[p]).toDouble()
        }
        return Math.sqrt(a / length)
    }

}

private data class RmsValues(
    val pos: Int,
    val index: Int,
    val rms: Double,
    val diff: Double
)

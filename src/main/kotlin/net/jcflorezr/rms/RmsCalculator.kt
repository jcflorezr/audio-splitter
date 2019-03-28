package net.jcflorezr.rms

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.jcflorezr.broker.Topic
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.util.AudioUtils
import net.jcflorezr.util.PropsUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface RmsCalculator {
    suspend fun generateRmsInfo(audioSignal: AudioSignal)
}

@Service
class RmsCalculatorImpl : RmsCalculator {

    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioSignalsRmsInfo>

    private val logger = KotlinLogging.logger { }

    companion object {
        private const val SILENCE_THRESHOLD = 0.001
        private const val ACTIVE_THRESHOLD = 0.03
    }

    override suspend fun generateRmsInfo(audioSignal: AudioSignal) {
        logger.info { "[${PropsUtils.getTransactionId(audioSignal.audioFileName)}][3][RMS] " +
            "Start to generate the Root Mean Square (RMS) of audio signal start: ${audioSignal.initialPositionInSeconds} - " +
            "end: ${audioSignal.endPositionInSeconds}." }

        val sampleRate = audioSignal.sampleRate
        val segmentSize = sampleRate / 10 // 0.1 secs
        val signal = audioSignal.data[0]!! // always mono
        val rmsSignalsInfo = ArrayList<AudioSignalRmsInfo>()
        generateSequence(RmsValues(pos = 0, index = 1, rms = 0.0, diff = 0.0)) {
            // If the last segment is less than 2/3 of the segment size, we include it in the previous segment.
            val endPos = if (it.pos + segmentSize * 5 / 3 > signal.size) signal.size else it.pos + segmentSize
            val currentRms = AudioUtils.millisecondsFormat(
                    value = computeRms(signal = signal, startPosition = it.pos, length = endPos - it.pos)
                )
            val positionInSeconds = it.pos.toFloat() / sampleRate
            val currentDiff = AudioUtils.millisecondsFormat(value = (it.rms - currentRms))
            val deepDiff = AudioUtils.millisecondsFormat(value = (it.diff - currentDiff))
            val silence = Math.abs(currentDiff) <= SILENCE_THRESHOLD
            val active = Math.abs(deepDiff) >= ACTIVE_THRESHOLD
            val initialPosInSeconds = positionInSeconds + audioSignal.initialPositionInSeconds
            val rmsSignalInfo = AudioSignalRmsInfo(
                audioFileName = audioSignal.audioFileName,
                index = AudioUtils.tenthsSecondsFormat(initialPosInSeconds.toDouble()),
                rms = currentRms,
                audioLength = audioSignal.totalFrames,
                sampleRate = sampleRate,
                initialPosition = it.pos + audioSignal.initialPosition,
                initialPositionInSeconds = initialPosInSeconds,
                segmentSize = segmentSize,
                segmentSizeInSeconds = AudioUtils.tenthsSecondsFormat(segmentSize / sampleRate.toDouble()).toFloat(),
                silence = silence,
                active = active
            )
            rmsSignalsInfo.add(rmsSignalInfo)
            RmsValues(pos = endPos, index = it.index + 1, rms = currentRms, diff = currentDiff)
        }.takeWhile { it.pos < signal.size }
        .toList()
        logger.info { "[${PropsUtils.getTransactionId(audioSignal.audioFileName)}][3][RMS] " +
            "Root Mean Square (RMS) for audio signals ==> start: ${rmsSignalsInfo.first().initialPositionInSeconds} - " +
            "end: ${rmsSignalsInfo.last().initialPositionInSeconds} were generated." }
        audioSignalRmsTopic.postMessage(message = AudioSignalsRmsInfo(rmsSignalsInfo))
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

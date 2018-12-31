package net.jcflorezr.signal

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.jcflorezr.broker.MessageLauncher
import net.jcflorezr.broker.Topic
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.util.AudioUtilsKt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface SoundZonesDetector {
    fun generateSoundZones(audioRmsInfoList: List<AudioSignalRmsInfoKt>)
}

@Service
class SoundZonesDetectorImpl : SoundZonesDetector {

    @Autowired
    private lateinit var messageLauncher: MessageLauncher<AudioClipInfo>

    companion object {
        private const val MAX_ACTIVE_COUNTER = 80
    }

    override fun generateSoundZones(audioRmsInfoList: List<AudioSignalRmsInfoKt>) {
        var silenceCounter = 0
        var activeCounter = 0
        var startActiveZonePosition = 0
        var endActiveZonePosition: Int
        val rmsSignalIterator = audioRmsInfoList.listIterator()
        while (rmsSignalIterator.hasNext()) {
            val rmsInfo = rmsSignalIterator.next()
            val isLastSegment = !rmsSignalIterator.hasNext()
            if (rmsInfo.silence || isLastSegment) {
                if (++silenceCounter == 2 || isLastSegment) {
                    if (activeCounter > 2) {
                        if (activeCounter >= MAX_ACTIVE_COUNTER) {
                            val from = startActiveZonePosition / rmsInfo.segmentSize
                            val to = rmsInfo.initialPosition / rmsInfo.segmentSize
                            getSoundZonesByActiveSegments(audioRmsInfoList.subList(from, to), rmsInfo.segmentSize)
                        } else {
                            startActiveZonePosition = Math.max(startActiveZonePosition - rmsInfo.segmentSize * 2, 0)
                            endActiveZonePosition = rmsInfo.initialPosition

                            val audioClipInfo = generateAudioClipInfo(startActiveZonePosition, endActiveZonePosition, rmsInfo)
                            messageLauncher.launchMessage(msg = audioClipInfo)
                        }
                    } else {
                        startActiveZonePosition = rmsInfo.initialPosition
                    }
                    activeCounter = 0
                } else if (silenceCounter < 2) {
                    activeCounter++
                }
            } else {
                if (++activeCounter == 1) {
                    startActiveZonePosition = rmsInfo.initialPosition
                }
                silenceCounter = 0
            }
        }
    }

    private fun getSoundZonesByActiveSegments(rmsInfoList: List<AudioSignalRmsInfoKt>, segmentSize: Int) {
        var activeCounter = 0
        var inactiveCounter = 0
        var startActiveZonePosition = 0
        var endActiveZonePosition: Int
        val rmsSignalIterator = rmsInfoList.listIterator()
        while (rmsSignalIterator.hasNext()) {
            val rmsInfo = rmsSignalIterator.next()
            val isLastSegment = !rmsSignalIterator.hasNext()
            if (rmsInfo.active && !isLastSegment) {
                if (++activeCounter == 3) {
                    startActiveZonePosition = rmsInfo.initialPosition
                    if (startActiveZonePosition > segmentSize * 4) {
                        startActiveZonePosition -= segmentSize * 4
                    }
                }
                inactiveCounter = 0
            } else if (++inactiveCounter == 3 || isLastSegment) {
                if (activeCounter >= 3) {
                    endActiveZonePosition = rmsInfo.initialPosition
                    val audioClipInfo = generateAudioClipInfo(startActiveZonePosition, endActiveZonePosition, rmsInfo)
                    // TODO: replace this with coroutine
                    messageLauncher.launchMessage(msg = audioClipInfo)
                }
                activeCounter = 0
            }
        }
    }

    private fun generateAudioClipInfo(startPosition: Int, endPosition: Int, rmsInfo: AudioSignalRmsInfoKt): AudioClipInfo {
        val samplingRate = rmsInfo.samplingRate
        val startPositionInSeconds = AudioUtilsKt.millisecondsFormat(startPosition.toFloat() / samplingRate)
        val endPositionInSeconds = AudioUtilsKt.millisecondsFormat(endPosition.toFloat() / samplingRate)
        val startPositionInSecondsInt = startPositionInSeconds.toInt()
        val suggestedAudioClipName = getSuggestedAudioClipName(
            startPositionInSeconds = startPositionInSeconds,
            audioLength = rmsInfo.audioLength,
            samplingRate = rmsInfo.samplingRate
        )
        return AudioClipInfo(
            audioFileName = rmsInfo.audioFileName,
            index = startPositionInSeconds,
            startPosition = startPosition,
            startPositionInSeconds = startPositionInSeconds,
            endPosition = endPosition,
            endPositionInSeconds = endPositionInSeconds,
            hours = startPositionInSecondsInt / 3600,
            minutes = startPositionInSecondsInt % 3600 / 60,
            seconds = startPositionInSecondsInt % 60,
            tenthsOfSecond = suggestedAudioClipName.substringAfter("_").toInt(),
            audioClipName = suggestedAudioClipName
        )
    }

    private fun getSuggestedAudioClipName(
        startPositionInSeconds: Float,
        audioLength: Int,
        samplingRate: Int
    ): String = startPositionInSeconds.toString().replace(".", "_").let {
        it.replaceBefore("_", it.substringBefore("_").format(getNumOfDigitsFormat(audioLength, samplingRate)))
    }

    private fun getNumOfDigitsFormat(
        audioLength: Int,
        samplingRate: Int
    ) = "%0" + Math.round(audioLength.toFloat() / samplingRate).toString().length + "d"

}

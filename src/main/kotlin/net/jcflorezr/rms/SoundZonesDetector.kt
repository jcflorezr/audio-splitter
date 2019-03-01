package net.jcflorezr.rms

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import net.jcflorezr.broker.Topic
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.util.AudioUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.util.*
import javax.annotation.PostConstruct

sealed class SoundDetectorAction
data class AudioSignalRmsArrived(val audioSignalRms: AudioSignalRmsInfo) : SoundDetectorAction()
data class DetectSoundZones(
        val audioSignalRms: AudioSignalRmsInfo,
        val soundZonesDetector: SoundZonesDetector
) : SoundDetectorAction()

interface SoundZonesDetectorActor {
    fun getActorForDetectingSoundZones() : SendChannel<SoundDetectorAction>
}

@Service
class SoundZonesDetectorActorImpl : SoundZonesDetectorActor {

    @Autowired
    private lateinit var soundZonesDetectorFactory: () -> SoundZonesDetector
    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao

    private lateinit var mainActor: SendChannel<SoundDetectorAction>
    private lateinit var actorsForAudioFiles: HashMap<String, Pair<SendChannel<SoundDetectorAction>, SoundZonesDetector>>

    @PostConstruct
    fun init() {
        mainActor = createMainActor()
        actorsForAudioFiles = HashMap()
    }

    override fun getActorForDetectingSoundZones(): SendChannel<SoundDetectorAction> = mainActor

    private fun createMainActor() = CoroutineScope(Dispatchers.Default).actor<SoundDetectorAction> {
        consumeEach { message ->
            when (message) {
                is AudioSignalRmsArrived -> assignActorForAudioFile(sampleAudioRmsInfo = message.audioSignalRms)
                else -> throw RuntimeException("No operation supported by SoundZonesDetectorActor. Operation: ${message::class.java}")
            }
        }
    }

    private fun createActorForAudioFile() = CoroutineScope(Dispatchers.Default).actor<SoundDetectorAction> {
        consumeEach { message ->
            when (message) {
                is DetectSoundZones -> detectSoundZones(message.audioSignalRms, message.soundZonesDetector)
                else -> throw RuntimeException("No operation supported by SoundZonesDetectorActor. Operation: ${message::class.java}")
            }
        }
    }

    private suspend fun assignActorForAudioFile(sampleAudioRmsInfo: AudioSignalRmsInfo) {
        val actorConfigForCurrentAudioFile = actorsForAudioFiles.computeIfAbsent(sampleAudioRmsInfo.audioFileName) {
            Pair(createActorForAudioFile(), soundZonesDetectorFactory())
        }
        actorConfigForCurrentAudioFile.first.send(DetectSoundZones(sampleAudioRmsInfo, actorConfigForCurrentAudioFile.second))
    }

    private suspend fun detectSoundZones(sampleAudioRmsInfo: AudioSignalRmsInfo, soundZonesDetector: SoundZonesDetector) {
        audioSignalRmsDao.retrieveAllAudioSignalsRms(
            key = "${sampleAudioRmsInfo.entityName}_${sampleAudioRmsInfo.audioFileName}"
        ).takeIf {
            it.isNotEmpty() && !it.first().isLastSegment() && it.first().initialPosition == soundZonesDetector.whereToStart()
        }
        ?.toList()
        ?.let { soundZonesDetector.detectSoundZones(audioRmsInfoList = it) }
    }

}


class SoundZonesDetector {

    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>
    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao

    private val detectorVariables = DetectorVariables()
    private var silenceCounter = 0
    private var activeCounter = 0
    private var inactiveCounter = 0
    private var startActiveZonePosition = 0
    private var endActiveZonePosition = 0
    private var silenceContinueFrom = 0.0
    private var activeContinueFrom = 0.0
    private var whereToStart = 0
    private var consecutiveNumber = 0
    private val lastClipProcessed = ArrayDeque<AudioClipInfo>()

    companion object {
        private const val MAX_ACTIVE_COUNTER = 80
    }

    suspend fun detectSoundZones(audioRmsInfoList: List<AudioSignalRmsInfo>) {

        // TODO: seek a proper way to debug the signal detector process
        println("STARTING ----- ${audioRmsInfoList.first().audioFileName} silCnt: $silenceCounter - silConFrom: $silenceContinueFrom - " +
                "startAct: $startActiveZonePosition - inacCnt: $inactiveCounter - actConFrom: $activeContinueFrom - whToSt: $whereToStart")

        if (activeContinueFrom != 0.0) {
            getSoundZonesByActiveSegments(audioRmsInfoList)
        }
        val rmsSignalIterator = getSignalIterator(audioRmsInfoList, silenceContinueFrom)
        while (rmsSignalIterator.hasNext()) {
            val rmsInfo = rmsSignalIterator.next()
            if (!rmsSignalIterator.nextIsConsecutive(rmsInfo)) {
                silenceContinueFrom = rmsInfo.index
                break
            }
            val isLastSegment = rmsInfo.isLastSegment()
            if (rmsInfo.silence || isLastSegment) {
                if (++silenceCounter == 2 || isLastSegment) {
                    if (activeCounter > 2) {
                        if (activeCounter >= MAX_ACTIVE_COUNTER) {
                            silenceCounter = 0
                            val from = audioRmsInfoList.binarySearchBy(startActiveZonePosition) {it.initialPosition}.takeIf { it > 0 } ?: 0
                            val to = audioRmsInfoList.binarySearchBy(rmsInfo.initialPosition) {it.initialPosition}.takeIf { it > from } ?: audioRmsInfoList.size
                            getSoundZonesByActiveSegments(audioRmsInfoList.subList(from, to))
                        } else {
                            startActiveZonePosition = Math.max(startActiveZonePosition - rmsInfo.segmentSize * 2, 0)
                            endActiveZonePosition = rmsInfo.initialPosition

                            // TODO: seek a proper way to debug the signal detector process
                            println("SILENCE ----- ${audioRmsInfoList.first().audioFileName} silCnt: $silenceCounter - silConFrom: $silenceContinueFrom - " +
                                "startAct: $startActiveZonePosition - endAct: $endActiveZonePosition - actCnt: $activeCounter - inacCnt: $inactiveCounter - actConFrom: $activeContinueFrom")

                            val audioClipInfo = generateAudioClipInfo(startActiveZonePosition, endActiveZonePosition, rmsInfo)
                            if (lastClipProcessed.isNotEmpty()) {
                                audioClipTopic.postMessage(message = lastClipProcessed.poll())
                            }
                            lastClipProcessed.offer(audioClipInfo)
                            audioSignalRmsDao.removeAudioSignalsRmsFromRange(
                                key = "${rmsInfo.entityName}_${rmsInfo.audioFileName}",
                                min = AudioUtils.tenthsSecondsFormat(AudioUtils.tenthsSecondsFormat(whereToStart.toDouble()) / rmsInfo.sampleRate.toDouble()),
                                max = AudioUtils.tenthsSecondsFormat(audioClipInfo.endPositionInSeconds.toDouble())
                            )
                            whereToStart = audioClipInfo.endPosition + rmsInfo.segmentSize
                            detectorVariables.startActiveZonePosition = startActiveZonePosition
                            detectorVariables.endActiveZonePosition = endActiveZonePosition
                            detectorVariables.silenceCounter = silenceCounter
                            detectorVariables.silenceContinueFrom = silenceContinueFrom
                            detectorVariables.activeCounter = 0
                            detectorVariables.inactiveCounter = inactiveCounter
                            silenceContinueFrom = 0.0
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
            if (!rmsSignalIterator.hasNext()) {
                // TODO: seek a proper way to debug the signal detector process
                println("RAN OUT of RMS ----- ${audioRmsInfoList.first().audioFileName} ${rmsInfo.index} silCnt: $silenceCounter - silConFrom: $silenceContinueFrom - " +
                        "startAct: $startActiveZonePosition - endAct: $endActiveZonePosition - actCnt: $activeCounter - inacCnt: $inactiveCounter - actConFrom: $activeContinueFrom")
                silenceCounter = detectorVariables.silenceCounter
                activeCounter = detectorVariables.activeCounter
                inactiveCounter = detectorVariables.inactiveCounter
                startActiveZonePosition = detectorVariables.startActiveZonePosition
                endActiveZonePosition = detectorVariables.endActiveZonePosition
                silenceContinueFrom = 0.0
                if (isLastSegment) {
                    if (lastClipProcessed.isEmpty() || lastClipProcessed.size > 1) {
                        throw RuntimeException("there are ${lastClipProcessed.size} last clips") // TODO: implement custom exception
                    }
                    val lastClip = lastClipProcessed.poll()
                    lastClip.lastClip = true
                    audioClipTopic.postMessage(message = lastClip)
                    val remainingAudioRms = audioSignalRmsDao.retrieveAllAudioSignalsRms("${rmsInfo.entityName}_${rmsInfo.audioFileName}")
                    if (remainingAudioRms.isNotEmpty()) {
                        println("I AM DONE! NOW REMOVING REMAINING RMS ROWS ----- ${remainingAudioRms.first().audioFileName} firstRem: ${remainingAudioRms.first().index} " +
                            "lastRem: ${remainingAudioRms.last().index} - currentRms: ${rmsInfo.index}")
                        audioSignalRmsDao.removeAudioSignalsRmsFromRange(
                            key = "${rmsInfo.entityName}_${rmsInfo.audioFileName}",
                            min = AudioUtils.tenthsSecondsFormat(remainingAudioRms.first().index),
                            max = AudioUtils.tenthsSecondsFormat(rmsInfo.index)
                        )
                    }
                }
            }

        }
    }

    private suspend fun getSoundZonesByActiveSegments(audioRmsInfoList: List<AudioSignalRmsInfo>) {
        if (activeContinueFrom == 0.0) {
            activeCounter = 0
            inactiveCounter = 0
            startActiveZonePosition = 0
            endActiveZonePosition = 0
        }
        val rmsSignalIterator = getSignalIterator(audioRmsInfoList, activeContinueFrom)
        val segmentSize = audioRmsInfoList.first().segmentSize
        while (rmsSignalIterator.hasNext()) {
            val rmsInfo = rmsSignalIterator.next()
            if (!rmsSignalIterator.nextIsConsecutive(rmsInfo)) {
                activeContinueFrom = rmsInfo.index
                break
            }
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
                    // TODO: seek a proper way to debug the signal detector process
                    println("ACTIVE ----- ${audioRmsInfoList.first().audioFileName} silCnt: $silenceCounter - silConFrom: $silenceContinueFrom - " +
                        "startAct: $startActiveZonePosition - endAct: $endActiveZonePosition - actCnt: $activeCounter - inacCnt: $inactiveCounter - actConFrom: $activeContinueFrom")
                    val audioClipInfo = generateAudioClipInfo(startActiveZonePosition, endActiveZonePosition, rmsInfo)
                    if (lastClipProcessed.isNotEmpty()) {
                        audioClipTopic.postMessage(message = lastClipProcessed.poll())
                    }
                    lastClipProcessed.offer(audioClipInfo)
                    audioSignalRmsDao.removeAudioSignalsRmsFromRange(
                        key = "${rmsInfo.entityName}_${rmsInfo.audioFileName}",
                        min = AudioUtils.tenthsSecondsFormat(whereToStart.toDouble() / rmsInfo.sampleRate),
                        max = AudioUtils.tenthsSecondsFormat(audioClipInfo.endPositionInSeconds)
                    )
                    whereToStart = audioClipInfo.endPosition + rmsInfo.segmentSize
                    detectorVariables.startActiveZonePosition = startActiveZonePosition
                    detectorVariables.endActiveZonePosition = endActiveZonePosition
                    detectorVariables.silenceCounter = silenceCounter
                    detectorVariables.silenceContinueFrom = silenceContinueFrom
                    detectorVariables.activeCounter = 0
                    detectorVariables.inactiveCounter = inactiveCounter
                    activeContinueFrom = 0.0
                }
                activeCounter = 0
            }
        }
        if (!rmsSignalIterator.hasNext()) {
            activeContinueFrom = 0.0
            silenceCounter = detectorVariables.silenceCounter
            activeCounter = detectorVariables.activeCounter
            inactiveCounter = detectorVariables.inactiveCounter
            startActiveZonePosition = detectorVariables.startActiveZonePosition
            endActiveZonePosition = detectorVariables.endActiveZonePosition
        }
    }

    private fun getSignalIterator(
            audioRmsInfoList: List<AudioSignalRmsInfo>,
            continueFrom: Double
    ): ListIterator<AudioSignalRmsInfo> = if (continueFrom == 0.0) {
        audioRmsInfoList.listIterator()
    } else {
        val continueFromIndex = audioRmsInfoList.binarySearchBy(AudioUtils.tenthsSecondsFormat(silenceContinueFrom)) { it.index }
        audioRmsInfoList.subList(continueFromIndex, audioRmsInfoList.size).listIterator()
    }

    private fun ListIterator<AudioSignalRmsInfo>.nextIsConsecutive(
        current: AudioSignalRmsInfo
    ): Boolean {
        if (!this.hasPrevious() || !this.hasNext()) {
            return true
        } else {
            val next = this.next()
            if (current.initialPosition + current.segmentSize == next.initialPosition) {
                this.previous()
                return true
            }
            return false
        }
    }

    private fun generateAudioClipInfo(startPosition: Int, endPosition: Int, rmsInfo: AudioSignalRmsInfo): AudioClipInfo {
        val sampleRate = rmsInfo.sampleRate
        val startPositionInSeconds = AudioUtils.millisecondsFormat(startPosition.toFloat() / sampleRate)
        val endPositionInSeconds = AudioUtils.millisecondsFormat(endPosition.toFloat() / sampleRate)
        val startPositionInSecondsInt = startPositionInSeconds.toInt()
        val suggestedAudioClipName = getSuggestedAudioClipName(
            startPositionInSeconds = startPositionInSeconds,
            audioLength = rmsInfo.audioLength,
            sampleRate = sampleRate
        )
        return AudioClipInfo(
            audioFileName = rmsInfo.audioFileName,
            consecutive = ++consecutiveNumber,
            index = startPositionInSeconds,
            sampleRate = sampleRate,
            initialPosition = startPosition,
            initialPositionInSeconds = startPositionInSeconds,
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
        sampleRate: Int
    ): String = startPositionInSeconds.toString().replace(".", "_").let {
        it.replaceBefore("_", it.substringBefore("_").format(getNumOfDigitsFormat(audioLength, sampleRate)))
    }

    private fun getNumOfDigitsFormat(
        audioLength: Int,
        samplingRate: Int
    ) = "%0" + Math.round(audioLength.toFloat() / samplingRate).toString().length + "d"

    fun whereToStart() = whereToStart

    private data class DetectorVariables(
        var silenceCounter: Int = 0,
        var activeCounter: Int = 0,
        var inactiveCounter: Int = 0,
        var startActiveZonePosition: Int = 0,
        var endActiveZonePosition: Int = 0,
        var silenceContinueFrom: Double = 0.0,
        var activeContinueFrom: Double = 0.0,
        var whereToStart: Int = 0
    )

}


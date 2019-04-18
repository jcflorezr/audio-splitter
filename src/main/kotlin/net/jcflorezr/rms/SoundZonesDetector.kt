package net.jcflorezr.rms

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import mu.KotlinLogging
import net.jcflorezr.broker.Topic
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.exception.ActorException
import net.jcflorezr.exception.AudioClipException
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.util.AudioUtils
import net.jcflorezr.util.PropsUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.ArrayDeque
import javax.annotation.PostConstruct

sealed class SoundDetectorAction
data class AudioSignalRmsArrived(val audioSignalRms: AudioSignalRmsInfo) : SoundDetectorAction()
data class DetectSoundZones(
    val audioSignalRms: AudioSignalRmsInfo,
    val soundZonesDetector: SoundZonesDetector
) : SoundDetectorAction()

interface SoundZonesDetectorActor {
    fun getActorForDetectingSoundZones(): SendChannel<SoundDetectorAction>
}

@Service
class SoundZonesDetectorActorImpl : SoundZonesDetectorActor {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var soundZonesDetectorFactory: () -> SoundZonesDetector
    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao

    private val logger = KotlinLogging.logger { }

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
                else -> throw ActorException.unknownActorOperationException(
                    actorClass = this::class.java,
                    actorOperationClass = message::class.java
                )
            }
        }
    }

    private suspend fun assignActorForAudioFile(sampleAudioRmsInfo: AudioSignalRmsInfo) {
        val actorConfigForCurrentAudioFile = actorsForAudioFiles.computeIfAbsent(sampleAudioRmsInfo.audioFileName) {
            Pair(createActorForAudioFile(), soundZonesDetectorFactory())
        }
        actorConfigForCurrentAudioFile.first.send(DetectSoundZones(sampleAudioRmsInfo, actorConfigForCurrentAudioFile.second))
    }

    private fun createActorForAudioFile() = CoroutineScope(Dispatchers.Default).actor<SoundDetectorAction> {
        consumeEach { message ->
            when (message) {
                is DetectSoundZones -> detectSoundZones(message.audioSignalRms, message.soundZonesDetector)
                else -> throw ActorException.unknownActorOperationException(
                    actorClass = this::class.java,
                    actorOperationClass = message::class.java
                )
            }
        }
    }

    private suspend fun detectSoundZones(sampleAudioRmsInfo: AudioSignalRmsInfo, soundZonesDetector: SoundZonesDetector) {
        val transactionId = propsUtils.getTransactionId(sampleAudioRmsInfo.audioFileName)
        audioSignalRmsDao.retrieveAllAudioSignalsRms(
            key = "${sampleAudioRmsInfo.entityName}_${sampleAudioRmsInfo.audioFileName}"
        ).let {
            val whereToContinueIndex = it.whereToContinueIndex(soundZonesDetector.whereToContinue())
            val readyToWork = it.isNotEmpty() && !it.first().isLastSegment() && whereToContinueIndex >= 0
            it.logActorReadiness(transactionId, readyToWork, whereToContinueIndex)
            if (readyToWork) {
                soundZonesDetector.detectSoundZones(audioRmsInfoList = it)
            }
        }
    }

    private fun List<AudioSignalRmsInfo>.whereToContinueIndex(whereToContinue: Int) =
        takeIf { isNotEmpty() }?.let { binarySearchBy(whereToContinue) { it.initialPosition } } ?: -1

    private fun List<AudioSignalRmsInfo>.logActorReadiness(transactionId: String, readyToWork: Boolean, whereToContinueIndex: Int) {
        logger.info { "[$transactionId][4][sound-zones] Is Sound Zones Detector ready to start working? $readyToWork " }
        readyToWork.takeIf { !it }
        ?.let {
            val isNotEmpty = isNotEmpty()
            val isNotLastSegment = takeIf { isNotEmpty() }?.let { !first().isLastSegment() } ?: false
            val whereToContinue = takeIf { isNotEmpty() }?.let { "whereToContinue: list[$whereToContinueIndex]" } ?: ""
            logger.info { "[$transactionId][4][sound-zones] isNotEmpty: $isNotEmpty - isNotLastSegment: $isNotLastSegment - whereToContinue: $whereToContinue" }
        }
    }
}

/**
 * Sound Zones are detected by two methods:
 *
 * 1. By Silence Segments: when there is an active sound zone delimited by silence segments
 *    at the beginning and at the end.
 * 2. By Active Segments: this occurs when there is strong background noise that makes difficult
 *    the voice detection and this voice should be detected by "inactive" segments.
 */
class SoundZonesDetector {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>
    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao

    private val logger = KotlinLogging.logger { }

    private var silenceCounter = 0
    private var activeCounter = 0
    private var inactiveCounter = 0
    private var startActiveZonePosition = 0
    private var endActiveZonePosition = 0
    private var whereToContinue = 0
    private var consecutiveNumber = 0
    private val previousClipProcessed = ArrayDeque<AudioClipInfo>()

    enum class SoundZoneDetectionMethods(val methodName: String) {
        BY_SILENCE_SEGMENTS("Silence Segment"),
        BY_ACTIVE_SEGMENTS("Active Segment")
    }

    companion object {
        private const val MAX_ACTIVE_COUNTER = 80
    }

    private fun incrementSilenceCounter() = silenceCounter++
    private fun incrementActiveCounter() = activeCounter++
    private fun AudioSignalRmsInfo.initializeNewActiveZone() { startActiveZonePosition = initialPosition }

    suspend fun detectSoundZones(audioRmsInfoList: List<AudioSignalRmsInfo>) {
        val transactionId = propsUtils.getTransactionId(audioRmsInfoList.first().audioFileName)

        logIntroMessage(transactionId, audioRmsInfoList.first(), audioRmsInfoList.last(), SoundZoneDetectionMethods.BY_SILENCE_SEGMENTS)

        val rmsSignalIterator = getSignalIterator(audioRmsInfoList, whereToContinue)
        detectionLoop@while (rmsSignalIterator.hasNext()) {
            val currentRms = rmsSignalIterator.next()
            when {
                !rmsSignalIterator.nextIsConsecutive(currentRms) -> {
                    whereToContinue = currentRms.initialPosition
                    currentRms.logCurrentDetectionProcessInterruption(transactionId)
                    break@detectionLoop
                }
                currentRms.belongsToActiveZone() -> {
                    currentRms.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_SILENCE_SEGMENTS, "belongsToActiveZone()")
                    currentRms.startNewZoneOrContinueActiveZone()
                }
                else -> {
                    incrementSilenceCounter()
                    when {
                        currentRms.belongsToPossibleActiveZone() -> {
                            currentRms.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_SILENCE_SEGMENTS, "belongsToPossibleActiveZone()")
                            incrementActiveCounter()
                        }
                        soundZoneShouldBeDetectedByActiveSegments() -> {
                            currentRms.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_SILENCE_SEGMENTS, "soundZoneShouldBeDetectedByActiveSegments()")
                            currentRms.prepareToDetectSoundZoneByActiveSegments(audioRmsInfoList)
                            whereToContinue = currentRms.initialPosition + currentRms.segmentSize
                        }
                        soundZoneDetectedBySilenceSegments() -> {
                            currentRms.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_SILENCE_SEGMENTS, "soundZoneShouldBeDetectedByActiveSegments()")
                            currentRms.processSoundZoneDetectedBySilenceSegments(transactionId)
                        }
                        else -> {
                            currentRms.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_SILENCE_SEGMENTS, "none of these")
                            currentRms.initializeNewActiveZone()
                            activeCounter = 0
                        }
                    }
                }
            }
        }
        val lastRmsInList = audioRmsInfoList.last()
        lastRmsInList.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_SILENCE_SEGMENTS)
        lastRmsInList.finalizeCurrentDetectionProcess(
            transactionId = transactionId,
            rmsSignalIterator = rmsSignalIterator,
            detectionMethod = SoundZoneDetectionMethods.BY_SILENCE_SEGMENTS
        )
    }

    private suspend fun detectSoundZonesByActiveSegments(audioRmsInfoList: List<AudioSignalRmsInfo>) {
        val transactionId = propsUtils.getTransactionId(audioRmsInfoList.first().audioFileName)

        logIntroMessage(transactionId, audioRmsInfoList.first(), audioRmsInfoList.last(), SoundZoneDetectionMethods.BY_ACTIVE_SEGMENTS)

        val rmsSignalIterator = audioRmsInfoList.listIterator()
        while (rmsSignalIterator.hasNext()) {
            val currentRms = rmsSignalIterator.next()
            val isLastSegment = !rmsSignalIterator.hasNext()
            when {
                currentRms.belongsToActiveZoneInActiveSegment(isLastSegment) -> {
                    currentRms.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_ACTIVE_SEGMENTS, "belongsToActiveZoneInActiveSegment()")
                    currentRms.initializeActiveZoneInActiveSegment()
                }
                currentRms.belongsToPossibleActiveZoneInActiveSegment(isLastSegment) -> {
                    currentRms.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_ACTIVE_SEGMENTS, "belongsToPossibleActiveZoneInActiveSegment()")
                    inactiveCounter = 0
                }
                soundZoneDetectedByActiveSegments(isLastSegment) -> {
                    currentRms.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_ACTIVE_SEGMENTS, "soundZoneDetectedByActiveSegments()")
                    currentRms.processSoundZoneDetectedByActiveSegments(transactionId)
                }
                belongsToPossibleInactiveZoneInActiveSegment(isLastSegment) -> {
                    currentRms.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_ACTIVE_SEGMENTS, "belongsToPossibleInactiveZoneInActiveSegment()")
                    activeCounter = 0
                }
                else -> {
                    currentRms.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_ACTIVE_SEGMENTS, "none of these")
                }
            }
        }
        val lastRmsInList = audioRmsInfoList.last()
        lastRmsInList.logCurrentRms(transactionId, SoundZoneDetectionMethods.BY_ACTIVE_SEGMENTS)
        lastRmsInList.finalizeCurrentDetectionProcess(
            transactionId = transactionId,
            rmsSignalIterator = rmsSignalIterator,
            detectionMethod = SoundZoneDetectionMethods.BY_ACTIVE_SEGMENTS
        )
    }

    /*
    Current RMS belongs to active zone
     */

    // not implementing "rms.active" condition because there are cases when the current rms
    // is neither silence nor active
    private fun AudioSignalRmsInfo.belongsToActiveZone() = !silence && !isLastSegment()

    private fun AudioSignalRmsInfo.startNewZoneOrContinueActiveZone(): Boolean {
        silenceCounter = 0
        incrementActiveCounter()
        if (beginsNewActiveZone()) {
            initializeNewActiveZone()
        }
        return true
    }

    private fun beginsNewActiveZone() = activeCounter == 1

    /*
    Current RMS belongs to a possible active zone
     */

    private fun AudioSignalRmsInfo.belongsToPossibleActiveZone() = !isLastSegment() && silenceCounter < 2

    /*
    There are a lot of consecutive active segments so that the sound zones
    should be detected by active segments rather than by silence segments
     */

    private fun soundZoneShouldBeDetectedByActiveSegments() = activeCounter >= MAX_ACTIVE_COUNTER

    private suspend fun AudioSignalRmsInfo.prepareToDetectSoundZoneByActiveSegments(audioRmsInfoList: List<AudioSignalRmsInfo>) {
        val from = audioRmsInfoList.binarySearchBy(startActiveZonePosition) { it.initialPosition }.takeIf { it > 0 } ?: 0
        val to = audioRmsInfoList.binarySearchBy(initialPosition) { it.initialPosition }.takeIf { it > from } ?: audioRmsInfoList.size
        silenceCounter = 0
        activeCounter = 0
        startActiveZonePosition = 0
        endActiveZonePosition = 0
        detectSoundZonesByActiveSegments(audioRmsInfoList.subList(from, to))
    }

    /*
    There is a sound zone which was detected by silence segments
     */

    private fun soundZoneDetectedBySilenceSegments() = activeCounter > 2

    private suspend fun AudioSignalRmsInfo.processSoundZoneDetectedBySilenceSegments(transactionId: String) {
        processSoundZoneDetected(transactionId, SoundZoneDetectionMethods.BY_SILENCE_SEGMENTS)
    }

    private suspend fun AudioSignalRmsInfo.processSoundZoneDetectedByActiveSegments(transactionId: String) {
        inactiveCounter++
        processSoundZoneDetected(transactionId, SoundZoneDetectionMethods.BY_ACTIVE_SEGMENTS)
    }

    private suspend fun AudioSignalRmsInfo.processSoundZoneDetected(transactionId: String, detectionMethod: SoundZoneDetectionMethods) {
        if (detectionMethod == SoundZoneDetectionMethods.BY_SILENCE_SEGMENTS) {
            // setting the start position two segments before the current one
            startActiveZonePosition = Math.max(startActiveZonePosition - segmentSize * 2, 0)
        }
        endActiveZonePosition = initialPosition

        logSoundZoneDetectedMessage(transactionId = transactionId, segmentType = detectionMethod)

        activeCounter = 0
        val audioClipInfo = generateAudioClipInfo(startActiveZonePosition, endActiveZonePosition, this)
        if (previousClipProcessed.isNotEmpty()) {
            audioClipTopic.postMessage(message = previousClipProcessed.poll())
        }
        previousClipProcessed.offer(audioClipInfo)
        audioSignalRmsDao.removeAudioSignalsRmsFromRange(
            key = "${entityName}_$audioFileName",
            min = 0.0,
            max = audioClipInfo.endPositionInSeconds.toDouble()
        )
        whereToContinue = audioClipInfo.endPosition + segmentSize
    }

    /*
    Current RMS belongs to an active sound zone inside a large segment of Active RMSs
    */

    private fun AudioSignalRmsInfo.belongsToActiveZoneInActiveSegment(
        isLastSegment: Boolean
    ) = active && !isLastSegment && (++activeCounter == 3)

    private fun AudioSignalRmsInfo.initializeActiveZoneInActiveSegment() {
        inactiveCounter = 0
        startActiveZonePosition = if (initialPosition > segmentSize * 4) {
            initialPosition - (segmentSize * 4)
        } else {
            initialPosition
        }
    }

    /*
    Current RMS belongs to a possible active sound zone inside a large segment of Active RMSs
    */

    private fun AudioSignalRmsInfo.belongsToPossibleActiveZoneInActiveSegment(
        isLastSegment: Boolean
    ) = active && !isLastSegment

    /*
    Current RMS belongs to a possible inactive sound zone inside a large segment of Active RMSs
    */
    private fun belongsToPossibleInactiveZoneInActiveSegment(isLastSegment: Boolean) = ++inactiveCounter == 3 || isLastSegment

    /*
    There is a sound zone which was detected by active segments
     */
    private fun soundZoneDetectedByActiveSegments(
        isLastSegment: Boolean
    ) = (inactiveCounter == 2 || isLastSegment) && activeCounter >= 3

    private suspend fun AudioSignalRmsInfo.finalizeCurrentDetectionProcess(
        transactionId: String,
        rmsSignalIterator: ListIterator<AudioSignalRmsInfo>,
        detectionMethod: SoundZoneDetectionMethods
    ) {
        if (!rmsSignalIterator.hasNext()) {
            whereToContinue = initialPosition + segmentSize
            logPartialCompleteness(transactionId, detectionMethod)
            if (isLastSegment()) {
                finalizeEntireDetectionProcess(transactionId)
            }
        }
    }

    private suspend fun AudioSignalRmsInfo.finalizeEntireDetectionProcess(transactionId: String) {
        if (previousClipProcessed.isEmpty()) {
            throw AudioClipException.noPreviousClipProcessedException()
        }
        if (previousClipProcessed.size > 1) {
            throw AudioClipException.moreThanOnePreviousClipsProcessedException(previousClipProcessed.size)
        }
        val lastClip = previousClipProcessed.poll()
        lastClip.lastClip = true
        audioClipTopic.postMessage(message = lastClip)
        val remainingAudioRms = audioSignalRmsDao.retrieveAllAudioSignalsRms("${entityName}_$audioFileName")
        if (remainingAudioRms.isNotEmpty()) {

            logTotalCompleteness(transactionId, remainingAudioRms)

            audioSignalRmsDao.removeAudioSignalsRmsFromRange(
                key = "${entityName}_$audioFileName",
                min = AudioUtils.tenthsSecondsFormat(remainingAudioRms.first().index),
                max = AudioUtils.tenthsSecondsFormat(index)
            )
        }
    }

    private fun getSignalIterator(
        audioRmsInfoList: List<AudioSignalRmsInfo>,
        continueFrom: Int
    ): ListIterator<AudioSignalRmsInfo> = if (continueFrom == 0) {
        audioRmsInfoList.listIterator()
    } else {
        val continueFromIndex = audioRmsInfoList.binarySearchBy(continueFrom) { it.initialPosition }
        audioRmsInfoList.subList(continueFromIndex, audioRmsInfoList.size).listIterator()
    }

    private fun ListIterator<AudioSignalRmsInfo>.nextIsConsecutive(
        current: AudioSignalRmsInfo
    ): Boolean {
        if (!hasPrevious() || !hasNext()) {
            return true
        } else {
            val next = next()
            if (current.initialPosition + current.segmentSize == next.initialPosition) {
                previous()
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

    private fun convertToSeconds(intValue: Int, sampleRate: Int) =
        AudioUtils.tenthsSecondsFormat(intValue.toFloat() / sampleRate.toFloat())

    fun whereToContinue() = whereToContinue

    private fun logIntroMessage(
        transactionId: String,
        firstRms: AudioSignalRmsInfo,
        lastRms: AudioSignalRmsInfo,
        detectionMethod: SoundZoneDetectionMethods
    ) {
        logger.info { "[$transactionId][4][sound-zones] ${detectionMethod.methodName}. Detect sound zones from: ${firstRms.initialPositionInSeconds} - " +
            "to: ${lastRms.initialPositionInSeconds}" }
        logger.info { "[$transactionId][4][sound-zones] silenceCounter: $silenceCounter - " +
            "startActiveZonePosition: ${convertToSeconds(startActiveZonePosition, firstRms.sampleRate)}  - " +
            "inactiveCounter: $inactiveCounter - activeCounter: $activeCounter - whereToContinue: ${convertToSeconds(whereToContinue, firstRms.sampleRate)}" }
    }

    private fun AudioSignalRmsInfo.logCurrentDetectionProcessInterruption(transactionId: String) {
        logger.info { "[$transactionId][4][sound-zones] Detection process has been interrupted due to a " +
            "non consecutive RMS segment was found. Rms segment needed to continue: ${convertToSeconds(whereToContinue, sampleRate)}" }
    }

    private fun AudioSignalRmsInfo.logSoundZoneDetectedMessage(transactionId: String, segmentType: SoundZoneDetectionMethods) {
        logger.info { "[$transactionId][4][sound-zones] Sound Zone detected by ${segmentType.methodName} ==> " +
            "Start: ${convertToSeconds(startActiveZonePosition, sampleRate)} - " +
            "end: ${convertToSeconds(endActiveZonePosition, sampleRate)}" }
        logger.info { "[$transactionId][4][sound-zones] silenceCounter: $silenceCounter - activeCounter: $activeCounter - inactiveCounter: $inactiveCounter" }
    }

    private fun AudioSignalRmsInfo.logPartialCompleteness(transactionId: String, detectionMethod: SoundZoneDetectionMethods) {
        logger.info { "[$transactionId][4][sound-zones] ${detectionMethod.methodName}. All partial RMS have been processed. CurrentRms: $index" }
        logger.info { "[$transactionId][4][sound-zones] silenceCounter: $silenceCounter - activeCounter: $activeCounter - inactiveCounter: $inactiveCounter - " +
            "startActiveZonePosition: ${convertToSeconds(startActiveZonePosition, sampleRate)} - whereToContinue: ${convertToSeconds(whereToContinue, sampleRate)}" }
    }

    private fun AudioSignalRmsInfo.logTotalCompleteness(transactionId: String, remainingAudioRms: List<AudioSignalRmsInfo>) {
        logger.info { "[$transactionId][4][sound-zones] All RMS have been processed. CurrentRms: $index" }
        logger.info { "[$transactionId][4][sound-zones] Removing some remaining RMS from: " +
            "${remainingAudioRms.first().index} - to: ${remainingAudioRms.last().index}" }
    }

    private var firstRmsInCurrentZone: Float = 0.0f
    private var rmsListInCurrentZone: ArrayList<Triple<Float, SoundZoneDetectionMethods, String>> = ArrayList()

    private fun AudioSignalRmsInfo.logCurrentRms(transactionId: String, detectionMethod: SoundZoneDetectionMethods, rmsOperation: String? = null) {
        val lastRmsOperation = rmsListInCurrentZone.takeIf { it.isNotEmpty() }?.last()
        if (lastRmsOperation?.operationHasChanged(detectionMethod, rmsOperation) == true) {
            if (rmsListInCurrentZone.isNotEmpty()) {
                val currentZoneInfo = "${rmsListInCurrentZone.map { it.first }} - " +
                    "${rmsListInCurrentZone.first().second.methodName} - ${rmsListInCurrentZone.first().third}"
                logger.info { "[$transactionId][4][sound-zones] $currentZoneInfo" }
                rmsListInCurrentZone = ArrayList()
            }
            if (rmsOperation != null) {
                rmsListInCurrentZone.add(Triple(initialPositionInSeconds, detectionMethod, rmsOperation))
                firstRmsInCurrentZone = initialPositionInSeconds
            }
        } else if (!isLastSegment()) {
            rmsListInCurrentZone.add(Triple(initialPositionInSeconds, detectionMethod, rmsOperation ?: "null operation"))
        }
    }

    private fun Triple<Float, SoundZoneDetectionMethods, String>.operationHasChanged(detectionMethod: SoundZoneDetectionMethods, rmsOperation: String?) =
        rmsListInCurrentZone.isEmpty() ||
        second != detectionMethod ||
        third != rmsOperation
}
package net.jcflorezr.clip

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import mu.KotlinLogging
import net.jcflorezr.broker.Topic
import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.exception.ActorException
import net.jcflorezr.exception.AudioClipException
import net.jcflorezr.exception.ExceptionHandler
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.util.AudioUtils
import net.jcflorezr.util.PropsUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

sealed class ClipAction
data class AudioClipInfoArrived(val audioClipInfo: AudioClipInfo) : ClipAction()
data class GenerateClips(
    val audioClipInfo: AudioClipInfo,
    val clipGenerator: ClipGenerator
) : ClipAction()


interface ClipGeneratorActor {
    fun getActorForGeneratingClips() : SendChannel<ClipAction>
}

@Service
class ClipGeneratorActorImpl : ClipGeneratorActor {

    @Autowired
    private lateinit var exceptionHandler: ExceptionHandler
    @Autowired
    private lateinit var clipGeneratorFactory: () -> ClipGenerator
    @Autowired
    private lateinit var audioClipDao: AudioClipDao

    private val logger = KotlinLogging.logger { }

    private lateinit var mainActor: SendChannel<ClipAction>
    private lateinit var actorsForAudioFiles: HashMap<String, Pair<SendChannel<ClipAction>, ClipGenerator>>

    @PostConstruct
    fun init() {
        mainActor = createMainActor()
        actorsForAudioFiles = HashMap()
    }

    override fun getActorForGeneratingClips(): SendChannel<ClipAction> = mainActor

    private fun createMainActor() = CoroutineScope(Dispatchers.Default).actor<ClipAction> {
        consumeEach { message ->
            when (message) {
                is AudioClipInfoArrived -> assignActorForAudioFile(sampleAudioClipInfo = message.audioClipInfo)
                else -> throw ActorException.unknownActorOperationException(
                    actorClass = this::class.java,
                    actorOperationClass = message::class.java
                )
            }
        }
    }

    private fun createActorForAudioFile() = CoroutineScope(Dispatchers.Default).actor<ClipAction> {
        consumeEach { message ->
            when (message) {
                is GenerateClips -> kotlin.runCatching {
                        generateClips(message.audioClipInfo, message.clipGenerator)
                    }.onFailure {
                        exceptionHandler.handle(exception = it, sourceAudioFileName = message.audioClipInfo.audioFileName)
                    }
                else -> throw ActorException.unknownActorOperationException(
                    actorClass = this::class.java,
                    actorOperationClass = message::class.java
                )
            }
        }
    }

    private suspend fun assignActorForAudioFile(sampleAudioClipInfo: AudioClipInfo) {
        val actorConfigForCurrentAudioFile = actorsForAudioFiles.computeIfAbsent(sampleAudioClipInfo.audioFileName) {
            Pair(createActorForAudioFile(), clipGeneratorFactory())
        }
        actorConfigForCurrentAudioFile.first.send(GenerateClips(sampleAudioClipInfo, actorConfigForCurrentAudioFile.second))
    }

    private suspend fun generateClips(sampleAudioClipInfo: AudioClipInfo, clipGenerator: ClipGenerator) {
        audioClipDao.retrieveAllAudioClipsInfo(
            key = "${sampleAudioClipInfo.entityName}_${sampleAudioClipInfo.audioFileName}"
        ).takeIf { it.isNotEmpty() }
        ?.let { clipsInfoList ->
            val transactionId = PropsUtils.getTransactionId(clipsInfoList.first().audioFileName)
            val whereToStart = clipsInfoList.binarySearchBy(clipGenerator.getNextClipConsecutiveNumberNeeded()) { it.consecutive }
            logger.info { "[$transactionId][5][clip-info] Preparing Clips Info. " +
                "Number of existing clips ${clipsInfoList.size}. first: clip# ${clipsInfoList.first().consecutive}. last: clip# ${clipsInfoList.last().consecutive}. " +
                "List of clips to be processed. From: list[$whereToStart] to: list[${clipsInfoList.size - 1}] . " +
                "Next clip needed to continue process: ${clipGenerator.getNextClipConsecutiveNumberNeeded()}" }
            if (whereToStart >= 0) {
                clipGenerator.generateClips(audioClipsInfo = clipsInfoList.subList(whereToStart, clipsInfoList.size))
            }
        }
    }

}

class ClipGenerator {

    @Autowired
    private lateinit var audioClipSignalTopicSignal: Topic<AudioClipSignal>
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao
    @Autowired
    private lateinit var audioClipDao: AudioClipDao

    private val logger = KotlinLogging.logger { }

    companion object {
        private const val maxActiveSecondsAmount = 13.0f
        private const val maxDistanceBetweenClips = 5.0f
    }

    private var groupedAudioClipsInfo = ArrayList<AudioClipInfo>()
    private var groupedClipSecondsAmount = 0.0f
    private var previousAudioClipInfo: AudioClipInfo? = null
    private var groupNumber = 0
    private var nextClipConsecutiveNumberNeeded = 1

    suspend fun generateClips(audioClipsInfo: List<AudioClipInfo>) {
        val transactionId = PropsUtils.getTransactionId(audioClipsInfo.first().audioFileName)
        logger.info { "[$transactionId][5][clip-info] Starting to group clips: ${audioClipsInfo.map { it.consecutive to it.audioClipName }}" }
        val audioClipInfoIterator = audioClipsInfo.listIterator()
        clipLoop@while (audioClipInfoIterator.hasNext()) {
            val currentAudioClipInfo = audioClipInfoIterator.next()
            when {
                !currentAudioClipInfo.isConsecutive() -> {
                    nextClipConsecutiveNumberNeeded = previousAudioClipInfo!!.consecutive + 1
                    currentAudioClipInfo.logCurrentGenerationProcessInterruption(transactionId)
                    break@clipLoop
                }
                else -> {
                    val currentClipSecondsLength = currentAudioClipInfo.getClipLengthInSeconds()
                    when {
                        currentAudioClipInfo.lastClip -> {
                            currentAudioClipInfo.logCurrentClipInfo("isLastClip()", transactionId)
                            groupedAudioClipsInfo.add(currentAudioClipInfo)
                            generateClipsAndResetVariables(transactionId)
                        }
                        currentClipSecondsLength.maxClipGroupLengthExceeded() -> {
                            currentAudioClipInfo.logCurrentClipInfo("maxClipGroupLengthExceeded()", transactionId)
                            audioClipInfoIterator.previous()
                            generateClipsAndResetVariables(transactionId)
                        }
                        currentAudioClipInfo.clipGroupIsNotLongEnoughButIsReadyForGeneration() -> {
                            currentAudioClipInfo.logCurrentClipInfo("clipGroupIsNotLongEnoughButIsReadyForGeneration()", transactionId)
                            generateClipsAndResetVariables(transactionId)
                            currentAudioClipInfo.populateClipGroup()
                        }
                        else -> {
                            currentAudioClipInfo.logCurrentClipInfo("keepPopulatingCurrentClipGroup()", transactionId)
                            currentAudioClipInfo.populateClipGroup()
                        }
                    }
                }
            }
            if (!audioClipInfoIterator.hasNext()) {
                nextClipConsecutiveNumberNeeded = currentAudioClipInfo.consecutive + 1
            }
        }
    }

    private suspend fun generateClipsAndResetVariables(transactionId: String) {
        generateClips(groupedAudioClipsInfo, transactionId)
        groupedClipSecondsAmount = 0.0f
        groupedAudioClipsInfo = ArrayList()
        previousAudioClipInfo = null
    }

    private suspend fun generateClips(audioClipsInfo: ArrayList<AudioClipInfo>, transactionId: String) {
        val firstClip = audioClipsInfo.first()
        val lastClip = audioClipsInfo.last()

        logger.info { "[$transactionId][5][clip-info] New grouped clip has been constructed. " +
            "Grouped clip length: ${audioClipsInfo.takeIf { it.size == 1 }?.first()?.getClipLengthInSeconds() ?: groupedClipSecondsAmount } - " +
            "${audioClipsInfo.map { it.consecutive to it.audioClipName }}" }

        audioClipsInfo.map { clipInfo ->
            val minIndex = getNearestIndex(clipInfo.initialPositionInSeconds)
            val maxIndex = getNearestIndex(clipInfo.endPositionInSeconds)
            audioSignalDao.retrieveAudioSignalsFromRange(key = "audioSignal_${clipInfo.audioFileName}", min = minIndex, max = maxIndex)
            .takeIf { it.isNotEmpty() }
            ?.map { getSignalDataForAudioClipPart(audioSignal = it, clipInfo = clipInfo) }
            ?.reduce { signal1, signal2 -> signal1 + signal2 }
            ?: throw AudioClipException.noAudioSignalsFoundForCreatingAudioClip(clipInfo)
        }.reduce { signal1, signal2 -> signal1 + signal2 }
        .let {
            audioClipSignalTopicSignal.postMessage(
                AudioClipSignal(
                    sampleRate = firstClip.sampleRate,
                    signal = arrayOf(it),
                    audioClipName = firstClip.audioClipName,
                    audioFileName = firstClip.audioFileName,
                    hours = firstClip.hours,
                    minutes = firstClip.minutes,
                    seconds = firstClip.seconds,
                    tenthsOfSecond = firstClip.tenthsOfSecond
                )
            )
        }
        audioClipDao.persistGroupedAudioClipInfo(
            firstAudioClipInfo = firstClip,
            lastAudioClipInfo = lastClip
        )
        removeAudioSignalsAndClipsInfo(firstClip, lastClip)
    }

    private fun AudioClipInfo.getClipLengthInSeconds() =
        AudioUtils.tenthsSecondsFormat(endPositionInSeconds - initialPositionInSeconds).toFloat()

    private fun AudioClipInfo.isConsecutive() =
        if (previousAudioClipInfo == null) { true } else { this.consecutive - previousAudioClipInfo!!.consecutive == 1 }

    private fun AudioClipInfo.clipGroupIsNotLongEnoughButIsReadyForGeneration() =
        isNotNearToLastClipGenerated() && groupedAudioClipsInfo.isNotEmpty()

    private fun AudioClipInfo.isNotNearToLastClipGenerated() =
        if (previousAudioClipInfo == null) {
            false
        } else {
            AudioUtils.tenthsSecondsFormat(this.initialPositionInSeconds - previousAudioClipInfo!!.endPositionInSeconds) > maxDistanceBetweenClips
        }

    private fun getNearestIndex(positionInSeconds: Float) =
        AudioUtils.tenthsSecondsFormat(0.5 * Math.floor(Math.abs(positionInSeconds / 0.5)))

    private fun getSignalDataForAudioClipPart(audioSignal: AudioSignal, clipInfo: AudioClipInfo): FloatArray {
        val from = if (audioSignal.initialPosition < clipInfo.initialPosition) {
            0 + (clipInfo.initialPosition - audioSignal.initialPosition)
        } else 0
        val to = if (audioSignal.endPosition > clipInfo.endPosition) {
            audioSignal.data[0]!!.size - (audioSignal.endPosition - clipInfo.endPosition)
        } else {
            audioSignal.data[0]!!.size
        }
        return if (from != 0 || to != audioSignal.data.size) audioSignal.data[0]!!.copyOfRange(from, to) else audioSignal.data[0]!!
    }

    private fun Float.maxClipGroupLengthExceeded() = groupedClipSecondsAmount + this > maxActiveSecondsAmount

    private fun AudioClipInfo.populateClipGroup() {
        groupedAudioClipsInfo.add(this)
        groupedClipSecondsAmount += this.getClipLengthInSeconds()
        previousAudioClipInfo = this
    }

    private suspend fun removeAudioSignalsAndClipsInfo(firstClip: AudioClipInfo, lastClip: AudioClipInfo) {
        audioSignalDao.removeAudioSignalsFromRange(
            key = "audioSignal_${lastClip.audioFileName}",
            min = 0.0,
            max = getNearestIndex(lastClip.endPositionInSeconds)
        )
        audioClipDao.removeAudioClipInfoFromRange(
            key = "${firstClip.entityName}_${firstClip.audioFileName}",
            min = firstClip.initialPositionInSeconds.toDouble(),
            max = lastClip.initialPositionInSeconds.toDouble()
        )
    }

    fun getNextClipConsecutiveNumberNeeded() = nextClipConsecutiveNumberNeeded

    private fun AudioClipInfo.logCurrentGenerationProcessInterruption(transactionId: String) {
        logger.info { "[$transactionId][5][clip-info] Clip generation process has been interrupted due to a " +
            "non consecutive Clip Info was found ${consecutive to audioClipName}. " +
            "Next consecutive Clip needed to continue the process: $nextClipConsecutiveNumberNeeded" }
    }

    private fun AudioClipInfo.logCurrentClipInfo(clipOperation: String, transactionId: String) {
        logger.info { "[$transactionId][5][clip-info] $clipOperation - ${consecutive to audioClipName} - lastClip: $lastClip" }
    }

}

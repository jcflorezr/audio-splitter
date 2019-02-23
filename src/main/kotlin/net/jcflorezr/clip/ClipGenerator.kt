package net.jcflorezr.clip

import biz.source_code.dsp.model.AudioClipSignal
import biz.source_code.dsp.model.AudioSignalKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.jcflorezr.broker.Topic
import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.util.AudioUtilsKt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.RuntimeException
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
    private lateinit var clipGeneratorFactory: () -> ClipGenerator
    @Autowired
    private lateinit var audioClipDao: AudioClipDao

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
            }
        }
    }

    private fun createActorForAudioFile() = CoroutineScope(Dispatchers.Default).actor<ClipAction> {
        consumeEach { message ->
            when (message) {
                is GenerateClips -> generateClips(message.audioClipInfo, message.clipGenerator)
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
        ?.let {
            val whereToStart = it.binarySearchBy(clipGenerator.getLastClipConsecutiveProcessed()) { it.consecutive }
            println("whereToStart: $whereToStart - lastCons: ${clipGenerator.getLastClipConsecutiveProcessed()}")
            if (whereToStart >= 0) {
                clipGenerator.generateClips(audioClipsInfo = it.subList(whereToStart, it.size))
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

    companion object {
        private const val maxActiveSecondsAmount = 13.0f
        private const val maxDistanceBetweenClips = 5.0f
    }

    private var groupedAudioClipsInfo = ArrayList<AudioClipInfo>()
    private var groupedClipSecondsAmount = 0.0f
    private var previousAudioClipInfo: AudioClipInfo? = null
    private var groupNumber = 0
    private var lastConsecutiveClipProcessed = 1

    suspend fun generateClips(audioClipsInfo: List<AudioClipInfo>) = coroutineScope {
        val audioClipInfoIterator = audioClipsInfo.listIterator()
        while (audioClipInfoIterator.hasNext()) {
            val currentAudioClipInfo = audioClipInfoIterator.next()

            println("prev: ${previousAudioClipInfo?.consecutive} - curr: ${currentAudioClipInfo.consecutive}" +
                    " - groupedClipSecs: $groupedClipSecondsAmount")

            lastConsecutiveClipProcessed = currentAudioClipInfo.consecutive
            if (!checkIfCurrentIsConsecutive(previousAudioClipInfo, currentAudioClipInfo)) {
                lastConsecutiveClipProcessed = previousAudioClipInfo!!.consecutive + 1
                break
            }
            val currentClipSecondsLength =
                AudioUtilsKt.tenthsSecondsFormat(currentAudioClipInfo.endPositionInSeconds - currentAudioClipInfo.initialPositionInSeconds).toFloat()
            val currentIsNotNear = checkIfCurrentIsNotNear(previousAudioClipInfo, currentAudioClipInfo)
            if (currentAudioClipInfo.lastClip || groupedClipSecondsAmount + currentClipSecondsLength > maxActiveSecondsAmount) {

                println("New grouped clip ----> groupedSecsLength: $groupedClipSecondsAmount - currClipSecsLength: $currentClipSecondsLength" +
                    " - lastClip: ${currentAudioClipInfo.lastClip} - currIsNear: ${!currentIsNotNear}")


                launch {
                    audioClipDao.persistGroupedAudioClipInfo(
                        groupNumber = groupNumber++,
                        firstAudioClipInfo = groupedAudioClipsInfo.first(),
                        lastAudioClipInfo = groupedAudioClipsInfo.last()
                    )
                }
                if (currentAudioClipInfo.lastClip) {
                    groupedAudioClipsInfo.add(currentAudioClipInfo)
                } else {
                    audioClipInfoIterator.previous()
                }
                if (!currentIsNotNear) {
                    generateClips(groupedAudioClipsInfo)
                } else {
                    groupedClipSecondsAmount += currentClipSecondsLength
                }
                groupedClipSecondsAmount = 0.0f
                groupedAudioClipsInfo = ArrayList()
                previousAudioClipInfo = null

            } else {
                groupedAudioClipsInfo.add(currentAudioClipInfo)
                groupedClipSecondsAmount += currentClipSecondsLength
                previousAudioClipInfo = currentAudioClipInfo
            }
        }
    }

    private suspend fun generateClips(audioClipsInfo: ArrayList<AudioClipInfo>) = coroutineScope {
        val firstClip = audioClipsInfo.first()
        val lastClip = audioClipsInfo.last()


        println("how many clips are part of this grouped clip? ${audioClipsInfo.size}")


        audioClipsInfo.map { clipInfo ->
            val minIndex = getNearestIndex(clipInfo.initialPositionInSeconds)
            val maxIndex = getNearestIndex(clipInfo.endPositionInSeconds)

            println("retrieving signals from $minIndex to $maxIndex")

            audioSignalDao.retrieveAudioSignalsFromRange(key = "audioSignal_${clipInfo.audioFileName}", min = minIndex, max = maxIndex)
            .takeIf { it.isNotEmpty() }
            ?.map { getSignalDataForAudioClipPart(audioSignal = it, clipInfo = clipInfo) }
            ?.reduce { signal1, signal2 -> signal1 + signal2 }
            ?: throw RuntimeException("No audio signals found for audio clip: $clipInfo")
            // TODO: implement custom exception
        }.reduce { signal1, signal2 -> signal1 + signal2 }
        .let {
            println("sending signal .... ")
            launch {
                audioClipSignalTopicSignal.postMessage(
                    AudioClipSignal(
                        sampleRate = firstClip.sampleRate,
                        signal = arrayOf(it),
                        audioClipName = firstClip.audioClipName,
                        audioFileName = firstClip.audioFileName
                    )
                )
            }
        }
        removeAudioSignalsAndClipsInfo(firstClip, lastClip)
    }

    private fun checkIfCurrentIsConsecutive(
        previous: AudioClipInfo?,
        current: AudioClipInfo
    ) = if (previous == null) { true } else { current.consecutive - previous.consecutive == 1 }

    private fun checkIfCurrentIsNotNear(
        previous: AudioClipInfo?,
        current: AudioClipInfo
    ) = if (previous == null) {
        false
    } else {
        AudioUtilsKt.tenthsSecondsFormat(current.initialPositionInSeconds - previous.endPositionInSeconds) > maxDistanceBetweenClips
    }

    private fun getNearestIndex(positionInSeconds: Float) =
        AudioUtilsKt.tenthsSecondsFormat(0.5 * Math.floor(Math.abs(positionInSeconds / 0.5)))

    private fun getSignalDataForAudioClipPart(audioSignal: AudioSignalKt, clipInfo: AudioClipInfo): FloatArray {
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

    private fun removeAudioSignalsAndClipsInfo(firstClip: AudioClipInfo, lastClip: AudioClipInfo) {
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

    fun getLastClipConsecutiveProcessed() = lastConsecutiveClipProcessed

}


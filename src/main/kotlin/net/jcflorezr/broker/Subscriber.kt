package net.jcflorezr.broker

import mu.KotlinLogging
import net.jcflorezr.clip.AudioClipInfoArrived
import net.jcflorezr.clip.ClipGeneratorActor
import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.SourceFileDao
import net.jcflorezr.exception.ExceptionHandler
import net.jcflorezr.exception.SignalException
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.rms.AudioSignalRmsArrived
import net.jcflorezr.rms.RmsCalculator
import net.jcflorezr.rms.SoundZonesDetectorActor
import net.jcflorezr.signal.AudioIo
import net.jcflorezr.storage.BucketClient
import net.jcflorezr.util.AudioFormats
import net.jcflorezr.util.PropsUtils
import java.io.File
import java.nio.ByteBuffer

interface Subscriber<T : Message> {
    suspend fun update(message: T)
}

class SourceFileSubscriber(
    private val propsUtils: PropsUtils,
    private val exceptionHandler: ExceptionHandler,
    sourceFileTopic: Topic<InitialConfiguration>,
    private val audioIo: AudioIo,
    private val sourceFileDao: SourceFileDao
) : Subscriber<InitialConfiguration> {

    private val logger = KotlinLogging.logger { }

    init {
        sourceFileTopic.register(this)
    }

    override suspend fun update(message: InitialConfiguration) {
        val sourceAudioFileName = message.audioFileName
        logger.info { "[${propsUtils.getTransactionId(sourceAudioFileName)}][1][entry-point] " +
            "Message received with the initial configuration." }
        kotlin.runCatching {
            sourceFileDao.persistAudioFileMetadata(initialConfiguration = message)
            audioIo.generateAudioSignalFromAudioFile(configuration = message)
        }.onFailure {
            exceptionHandler.handle(exception = it, sourceAudioFileName = sourceAudioFileName)
        }
    }
}

class SignalSubscriber(
    private val propsUtils: PropsUtils,
    private val exceptionHandler: ExceptionHandler,
    signalTopic: Topic<AudioSignal>,
    private val rmsCalculator: RmsCalculator,
    private val audioSignalDao: AudioSignalDao
) : Subscriber<AudioSignal> {

    private val logger = KotlinLogging.logger { }

    init {
        signalTopic.register(this)
    }

    override suspend fun update(message: AudioSignal) {
        logger.info { "[${propsUtils.getTransactionId(message.audioFileName)}][2][audio-signal] " +
            "Message received with audio signal with index: ${message.index}." }
        kotlin.runCatching {
            audioSignalDao.persistAudioSignalPart(audioSignal = message)
            .takeIf {
                it.audioFileName == message.audioFileName &&
                it.index == message.index &&
                it.content == ByteBuffer.wrap(message.dataInBytes)
            }?.run {
                audioSignalDao.storeAudioSignal(audioSignal = message).takeIf { it }
                ?.let {
                    rmsCalculator.generateRmsInfo(audioSignal = message)
                }
            } ?: throw SignalException.signalPartNotStoredException(audioSignal = message)
        }.onFailure {
            exceptionHandler.handle(exception = it, sourceAudioFileName = message.audioFileName)
        }
    }
}

class SignalRmsSubscriber(
    private val propsUtils: PropsUtils,
    private val exceptionHandler: ExceptionHandler,
    audioSignalRmsTopic: Topic<AudioSignalsRmsInfo>,
    private val soundZonesDetectorActor: SoundZonesDetectorActor,
    private val audioSignalRmsDao: AudioSignalRmsDao
) : Subscriber<AudioSignalsRmsInfo> {

    private val logger = KotlinLogging.logger { }

    init {
        audioSignalRmsTopic.register(this)
    }

    override suspend fun update(message: AudioSignalsRmsInfo) {
        logger.info { "[${propsUtils.getTransactionId(message.audioSignals.first().audioFileName)}][3][RMS] " +
            "Message received with RMS for audio signals from: ${message.audioSignals.first().initialPositionInSeconds} - " +
            "to: ${message.audioSignals.last().initialPositionInSeconds}" }
        kotlin.runCatching {
            audioSignalRmsDao.storeAudioSignalsRms(message.audioSignals)
            soundZonesDetectorActor.getActorForDetectingSoundZones()
                .send(AudioSignalRmsArrived(audioSignalRms = message.audioSignals.first()))
        }.onFailure {
            exceptionHandler.handle(exception = it, sourceAudioFileName = message.audioSignals.first().audioFileName)
        }.getOrDefault(Unit)
    }
}

class AudioClipInfoSubscriber(
    private val propsUtils: PropsUtils,
    private val exceptionHandler: ExceptionHandler,
    audioClipInfoTopic: Topic<AudioClipInfo>,
    private val audioClipDao: AudioClipDao,
    private val clipGeneratorActor: ClipGeneratorActor
) : Subscriber<AudioClipInfo> {

    private val logger = KotlinLogging.logger { }

    init {
        audioClipInfoTopic.register(this)
    }

    override suspend fun update(message: AudioClipInfo) {
        logger.info { "[${propsUtils.getTransactionId(message.audioFileName)}][5][clip-info] " +
            "Message received with Clip Info ${message.consecutive to message.audioClipName}. " +
            "Start: ${message.initialPositionInSeconds} - end: ${message.endPositionInSeconds}" }
        kotlin.runCatching {
            audioClipDao.storeAudioClipInfo(audioClipInfo = message)
            clipGeneratorActor.getActorForGeneratingClips()
                .send(AudioClipInfoArrived(audioClipInfo = message))
        }.onFailure {
            exceptionHandler.handle(exception = it, sourceAudioFileName = message.audioFileName)
        }.getOrDefault(Unit)
    }
}

class AudioClipSignalSubscriber(
    private val propsUtils: PropsUtils,
    private val exceptionHandler: ExceptionHandler,
    private val bucketClient: BucketClient,
    audioClipSignalTopic: Topic<AudioClipSignal>,
    private val audioIo: AudioIo,
    private val audioSplitterProducer: AudioSplitterProducer
) : Subscriber<AudioClipSignal> {

    private val thisClass: Class<AudioClipSignalSubscriber> = this.javaClass
    private val tempDirectoryPath: String
    private val logger = KotlinLogging.logger { }

    init {
        audioClipSignalTopic.register(this)
        tempDirectoryPath = thisClass.getResource("/temp-converted-files").path
    }

    override suspend fun update(message: AudioClipSignal) {
        val transactionId = propsUtils.getTransactionId(message.audioFileName)
        logger.info { "[$transactionId][6][audio-clip] Message received with Grouped Clip Info (${message.audioClipName})." }
        kotlin.runCatching {
            val audioClipDirectoryPath = "$tempDirectoryPath/$transactionId"
            File(audioClipDirectoryPath)
            .takeIf { !it.exists() }
            ?.apply { mkdirs() }
            val audioClipPath = "$audioClipDirectoryPath/${message.audioClipName}"
            takeIf {
                audioIo.saveAudioFile(
                    fileName = audioClipPath,
                    extension = AudioFormats.FLAC.extension,
                    signal = message.signal,
                    sampleRate = message.sampleRate,
                    transactionId = transactionId
                )
            }?.let {
                bucketClient.uploadFileToBucket(File("$audioClipPath${AudioFormats.FLAC.extension}"), transactionId)
                audioSplitterProducer.sendAudioClip(audioClip = message, transactionId = transactionId)
            }
        }.onFailure {
            exceptionHandler.handle(exception = it, sourceAudioFileName = message.audioFileName)
        }
    }
}
package net.jcflorezr.broker

import kotlinx.coroutines.coroutineScope
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
import net.jcflorezr.util.AudioFormats
import net.jcflorezr.util.PropsUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.nio.ByteBuffer
import javax.annotation.PostConstruct

interface Subscriber<T : Message> {
    suspend fun update(message: T)
}

@Service
final class SourceFileSubscriber : Subscriber<InitialConfiguration> {

    @Autowired
    private lateinit var exceptionHandler: ExceptionHandler
    @Autowired
    private lateinit var sourceFileTopic: Topic<InitialConfiguration>
    @Autowired
    private lateinit var audioIo: AudioIo
    @Autowired
    private lateinit var sourceFileDao: SourceFileDao

    private val logger = KotlinLogging.logger { }

    @PostConstruct
    fun init() = sourceFileTopic.register(this)

    override suspend fun update(message: InitialConfiguration) {
        logger.info { "[${PropsUtils.getTransactionId(message.audioFileLocation)}][1][entry-point] " +
            "Message received with the initial configuration." }
        kotlin.runCatching {
            sourceFileDao.persistAudioFileMetadata(initialConfiguration = message)
            audioIo.generateAudioSignalFromAudioFile(configuration = message)
        }.onFailure {
            exceptionHandler.handle(exception = it, sourceAudioFileName = message.audioFileLocation)
        }
    }

}

@Service
final class SignalSubscriber : Subscriber<AudioSignal> {

    @Autowired
    private lateinit var exceptionHandler: ExceptionHandler
    @Autowired
    private lateinit var signalTopic: Topic<AudioSignal>
    @Autowired
    private lateinit var rmsCalculator: RmsCalculator
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    private val logger = KotlinLogging.logger { }

    @PostConstruct
    fun init() = signalTopic.register(this)

    override suspend fun update(message: AudioSignal) {
        logger.info { "[${PropsUtils.getTransactionId(message.audioFileName)}][2][audio-signal] " +
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

@Service
final class SignalRmsSubscriber : Subscriber<AudioSignalsRmsInfo> {

    @Autowired
    private lateinit var exceptionHandler: ExceptionHandler
    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioSignalsRmsInfo>
    @Autowired
    private lateinit var soundZonesDetectorActor: SoundZonesDetectorActor
    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao

    private val logger = KotlinLogging.logger { }

    @PostConstruct
    fun init() = audioSignalRmsTopic.register(this)

    override suspend fun update(message: AudioSignalsRmsInfo) {
        logger.info { "[${PropsUtils.getTransactionId(message.audioSignals.first().audioFileName)}][3][RMS] " +
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

@Service
final class AudioClipInfoSubscriber : Subscriber<AudioClipInfo> {

    @Autowired
    private lateinit var exceptionHandler: ExceptionHandler
    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>
    @Autowired
    private lateinit var audioClipDao: AudioClipDao
    @Autowired
    private lateinit var clipGeneratorActor: ClipGeneratorActor

    private val logger = KotlinLogging.logger { }

    @PostConstruct
    fun init() = audioClipTopic.register(this)

    override suspend fun update(message: AudioClipInfo) {
        logger.info { "[${PropsUtils.getTransactionId(message.audioFileName)}][5][clip-info] " +
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

@Service
final class AudioClipSignalSubscriber : Subscriber<AudioClipSignal> {

    @Autowired
    private lateinit var exceptionHandler: ExceptionHandler
    @Autowired
    private lateinit var audioClipSignalTopic: Topic<AudioClipSignal>
    @Autowired
    private lateinit var audioIo: AudioIo

    private val logger = KotlinLogging.logger { }

    @PostConstruct
    fun init() = audioClipSignalTopic.register(this)

    override suspend fun update(message: AudioClipSignal) {
        logger.info { "[${PropsUtils.getTransactionId(message.audioFileName)}][6][audio-clip] " +
            "Message received with Grouped Clip Info (${message.audioClipName})."}
        kotlin.runCatching {
            val outputDirectoryPath = PropsUtils.getDirectoryPath()
            val transactionId = PropsUtils.getTransactionId(message.audioFileName)
            val audioClipDirectoryPath = "$outputDirectoryPath/$transactionId"
            File(audioClipDirectoryPath)
            .takeIf { !it.exists() }
            ?.apply { mkdirs() }
            audioIo.saveAudioFile(
                fileName = "$audioClipDirectoryPath/${message.audioClipName}",
                extension = AudioFormats.FLAC.extension,
                signal = message.signal,
                sampleRate = message.sampleRate,
                transactionId = transactionId
            )
        }.onFailure {
            exceptionHandler.handle(exception = it, sourceAudioFileName = message.audioFileName)
        }

        // TODO: call transcriber through queue?
    }

}
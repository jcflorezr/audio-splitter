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
    private lateinit var propsUtils: PropsUtils
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

@Service
final class SignalSubscriber : Subscriber<AudioSignal> {

    @Autowired
    private lateinit var propsUtils: PropsUtils
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

@Service
final class SignalRmsSubscriber : Subscriber<AudioSignalsRmsInfo> {

    @Autowired
    private lateinit var propsUtils: PropsUtils
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

@Service
final class AudioClipInfoSubscriber : Subscriber<AudioClipInfo> {

    @Autowired
    private lateinit var propsUtils: PropsUtils
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

@Service
final class AudioClipSignalSubscriber : Subscriber<AudioClipSignal> {

    @Autowired
    private lateinit var bucketClient: BucketClient
    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var exceptionHandler: ExceptionHandler
    @Autowired
    private lateinit var audioClipSignalTopic: Topic<AudioClipSignal>
    @Autowired
    private lateinit var audioIo: AudioIo

    private val thisClass: Class<AudioClipSignalSubscriber> = this.javaClass
    private lateinit var tempDirectoryPath: String
    private val logger = KotlinLogging.logger { }

    @PostConstruct
    fun init() {
        audioClipSignalTopic.register(this)
        tempDirectoryPath = thisClass.getResource("/temp-converted-files").path
    }

    override suspend fun update(message: AudioClipSignal) {
        logger.info { "[${propsUtils.getTransactionId(message.audioFileName)}][6][audio-clip] " +
            "Message received with Grouped Clip Info (${message.audioClipName})." }
        val transactionId = propsUtils.getTransactionId(message.audioFileName)
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
            }
        }.onFailure {
            exceptionHandler.handle(exception = it, sourceAudioFileName = message.audioFileName)
        }.run {
            File("$tempDirectoryPath/${propsUtils.getSourceFileLocation(transactionId)}").delete()
            if (message.lastClip) {
                File("$tempDirectoryPath/$transactionId").deleteRecursively()
            }
        }

        // TODO: call transcriber through queue?
    }
}
package net.jcflorezr.broker

import net.jcflorezr.signal.AudioIo
import kotlinx.coroutines.coroutineScope
import net.jcflorezr.clip.AudioClipInfoArrived
import net.jcflorezr.clip.ClipGeneratorActor
import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.SourceFileDao
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.model.AudioSignalKt
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.rms.AudioSignalRmsArrived
import net.jcflorezr.rms.RmsCalculator
import net.jcflorezr.rms.SoundZonesDetectorActor
import net.jcflorezr.util.AudioFormats
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import javax.annotation.PostConstruct

interface Subscriber<T : Message> {
    suspend fun update(message: T)
}

@Service
final class SourceFileSubscriber : Subscriber<InitialConfiguration> {

    @Autowired
    private lateinit var sourceFileTopic: Topic<InitialConfiguration>
    @Autowired
    private lateinit var audioIo: AudioIo
    @Autowired
    private lateinit var sourceFileDao: SourceFileDao

    @PostConstruct
    fun init() = sourceFileTopic.register(this)

    override suspend fun update(message: InitialConfiguration) {
        sourceFileDao.storeAudioFileMetadata(
            initialConfiguration = message
        )
        audioIo.generateAudioSignalFromAudioFile(
            configuration = message
        )
    }

}

@Service
final class SignalSubscriber : Subscriber<AudioSignalKt> {

    @Autowired
    private lateinit var signalTopic: Topic<AudioSignalKt>
    @Autowired
    private lateinit var rmsCalculator: RmsCalculator
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    @PostConstruct
    fun init() = signalTopic.register(this)

    override suspend fun update(message: AudioSignalKt) {
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
        } ?: throw RuntimeException()
        // TODO: provide custom Exception
    }

}

@Service
final class SignalRmsSubscriber : Subscriber<AudioSignalsRmsInfo> {

    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioSignalsRmsInfo>
    @Autowired
    private lateinit var soundZonesDetectorActor: SoundZonesDetectorActor
    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao

    @PostConstruct
    fun init() = audioSignalRmsTopic.register(this)

    override suspend fun update(message: AudioSignalsRmsInfo) = coroutineScope {
        audioSignalRmsDao.storeAudioSignalsRms(message.audioSignals)
        soundZonesDetectorActor.getActorForDetectingSoundZones()
            .send(AudioSignalRmsArrived(audioSignalRms = message.audioSignals.first()))
    }

}

@Service
final class AudioClipInfoSubscriber : Subscriber<AudioClipInfo> {

    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>
    @Autowired
    private lateinit var audioClipDao: AudioClipDao
    @Autowired
    private lateinit var clipGeneratorActor: ClipGeneratorActor

    @PostConstruct
    fun init() = audioClipTopic.register(this)

    override suspend fun update(message: AudioClipInfo) {
        audioClipDao.storeAudioClipInfo(audioClipInfo = message)
        clipGeneratorActor.getActorForGeneratingClips()
            .send(AudioClipInfoArrived(audioClipInfo = message))
    }

}

@Service
final class AudioClipSignalSubscriber : Subscriber<AudioClipSignal> {

    @Autowired
    private lateinit var audioClipSignalTopic: Topic<AudioClipSignal>
    @Autowired
    private lateinit var audioIo: AudioIo

    companion object {
        private val flacAudioFormat = AudioFormats.FLAC
    }

    @PostConstruct
    fun init() = audioClipSignalTopic.register(this)

    override suspend fun update(message: AudioClipSignal) {
        // TODO: get the filename from properties file?
        audioIo.saveAudioFile(
            fileName = message.audioClipName,
            extension = flacAudioFormat.extension,
            signal = message.signal,
            sampleRate = message.sampleRate
        )

        // TODO: call transcriber through queue?
    }

}
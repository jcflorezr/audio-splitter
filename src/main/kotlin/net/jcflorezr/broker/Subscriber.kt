package net.jcflorezr.broker

import biz.source_code.dsp.model.AudioSignalKt
import biz.source_code.dsp.sound.AudioIo
import kotlinx.coroutines.coroutineScope
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.SourceFileDao
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.signal.AudioSignalRmsArrived
import net.jcflorezr.signal.RmsCalculator
import net.jcflorezr.signal.SoundZonesDetectorActor
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
        audioSignalDao.storeAudioSignalPart(audioSignal = message)
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
    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>

    @PostConstruct
    fun init() = audioSignalRmsTopic.register(this)

    override suspend fun update(message: AudioSignalsRmsInfo) = coroutineScope {
        audioSignalRmsDao.storeAudioSignalsRms(message.audioSignals)
        // TODO: persist the audio signal rms to cassandra
        soundZonesDetectorActor.getActorForDetectingSoundZones()
            .send(AudioSignalRmsArrived(audioSignalRms = message.audioSignals.first()))
    }

}

@Service
final class AudioClipSubscriber : Subscriber<AudioClipInfo> {

    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>

    @PostConstruct
    fun init() = audioClipTopic.register(this)

    override suspend fun update(message: AudioClipInfo) {
//        1. store the last audio clip received
//            (audio clip generator should retrieve the sound
//            zones from database and determine if they are enough to build a grouped audio clip)
    }

}
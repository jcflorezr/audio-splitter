package net.jcflorezr.broker

import biz.source_code.dsp.model.AudioSignalKt
import biz.source_code.dsp.sound.AudioIo
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.SourceFileDao
import net.jcflorezr.signal.RmsCalculator
import net.jcflorezr.signal.SoundZonesDetector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import javax.annotation.PostConstruct

interface Subscriber {
    suspend fun update()
}

@Service
final class SourceFileSubscriber : Subscriber {

    @Autowired
    private lateinit var sourceFileTopic: Topic<InitialConfiguration>
    @Autowired
    private lateinit var audioIo: AudioIo
    @Autowired
    private lateinit var sourceFileDao: SourceFileDao

    @PostConstruct
    fun init() = sourceFileTopic.register(this)

    override suspend fun update() {
        val initialConfiguration = sourceFileTopic.getMessage()
        sourceFileDao.storeAudioFileMetadata(
            initialConfiguration = initialConfiguration
        )
        audioIo.generateAudioSignalFromAudioFile(
            configuration = initialConfiguration
        )
    }

}

@Service
final class SignalSubscriber : Subscriber {

    @Autowired
    private lateinit var signalTopic: Topic<AudioSignalKt>
    @Autowired
    private lateinit var rmsCalculator: RmsCalculator
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    @PostConstruct
    fun init() = signalTopic.register(this)

    override suspend fun update() {
        val audioSignal = signalTopic.getMessage()
        audioSignalDao.storeAudioSignalPart(audioSignal = audioSignal)
        .takeIf {
            it.audioFileName == audioSignal.audioFileName &&
            it.index == audioSignal.index &&
            it.content == ByteBuffer.wrap(audioSignal.dataInBytes)
        }?.run {
            audioSignalDao.storeAudioSignal(audioSignal = audioSignal).takeIf { it }
            ?.let {
                rmsCalculator.generateRmsInfo(audioSignal = audioSignal)
            }
        } ?: throw RuntimeException()
        // TODO: provide custom Exception
    }

}

@Service
final class SignalRmsSubscriber : Subscriber {

    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioSignalRmsInfoKt>
    @Autowired
    private lateinit var soundZonesDetector: SoundZonesDetector
    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao

    @PostConstruct
    fun init() = audioSignalRmsTopic.register(this)

    override suspend fun update() {
        val audioSignalRms = audioSignalRmsTopic.getMessage()
        // TODO: implement custom exception
        audioSignalRmsDao.storeAudioSignalRms(audioSignalRms)
            .takeIf { it } ?: throw java.lang.RuntimeException()
//        val audioRmsInfoList =
//            audioSignalRmsDao.retrieveAllAudioSignalsRms(
//                key = audioSignalRms.entityName + "_" + audioSignalRms.audioFileName
//            ).toList()
//        soundZonesDetector.generateSoundZones(audioRmsInfoList)
    }

}

@Service
final class AudioClipSubscriber : Subscriber {

    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>

    @PostConstruct
    fun init() = audioClipTopic.register(this)

    override suspend fun update() {
        val audioClip = audioClipTopic.getMessage()
//        1. store the last audio clip received
//            (audio clip generator should retrieve the sound
//            zones from database and determine if they are enough to build a grouped audio clip)
    }

}
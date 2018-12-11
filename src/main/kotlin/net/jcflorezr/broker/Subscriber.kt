package net.jcflorezr.broker

import biz.source_code.dsp.model.AudioSignalKt
import biz.source_code.dsp.sound.AudioIo
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.persistence.AudioSignalDao
import net.jcflorezr.persistence.AudioSignalRmsDao
import net.jcflorezr.persistence.SourceFileDao
import net.jcflorezr.signal.RmsCalculator
import net.jcflorezr.signal.SoundZonesDetector
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

interface Subscriber {
    fun update()
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
    fun init() {
        sourceFileTopic.register(this)
    }

    override fun update() {
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
    fun init() {
        signalTopic.register(this)
    }

    override fun update() {
        val audioSignal = signalTopic.getMessage()
        audioSignalDao.storeAudioSignal(audioSignal = audioSignal).takeIf { it }
        ?.let {
            rmsCalculator.generateRmsInfo(audioSignal = audioSignal)
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
    fun init() {
        audioSignalRmsTopic.register(this)
    }

    override fun update() {
        val audioSignalRms = audioSignalRmsTopic.getMessage()
        audioSignalRmsDao.storeAudioSignalRms(audioSignalRms = audioSignalRms).takeIf { it }

        // TODO: we need to find a way to optimize calls to SoundZonesDetector
        //soundZonesDetector.getSoundZones(audioRmsInfoList = audioSignal)
    }

}

@Service
final class AudioClipSubscriber : Subscriber {

    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>

    @PostConstruct
    fun init() {
        audioClipTopic.register(this)
    }

    override fun update() {
        val audioClip = audioClipTopic.getMessage()
        // TODO: we need to find a way to group the audioClips
    }

}
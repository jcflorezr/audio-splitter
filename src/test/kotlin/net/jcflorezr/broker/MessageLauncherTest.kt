package net.jcflorezr.broker

import biz.source_code.dsp.model.AudioSignalKt
import kotlinx.coroutines.runBlocking
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.model.InitialConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

interface MessageLauncher<T : Message> {
    fun launchMessage(msg : T)
}

@Service
class SourceFileLauncherTest : MessageLauncher<InitialConfiguration> {

    @Autowired
    private lateinit var topic: Topic<InitialConfiguration>

    override fun launchMessage(msg: InitialConfiguration) {
        runBlocking { topic.postMessage(msg) }
    }
}

@Service
class AudioSignalLauncherTest : MessageLauncher<AudioSignalKt> {

    @Autowired
    private lateinit var topic: Topic<AudioSignalKt>

    override fun launchMessage(msg: AudioSignalKt) {
        runBlocking { topic.postMessage(msg) }
    }
}

@Service
class AudioSignalRmsLauncherTest : MessageLauncher<AudioSignalRmsInfoKt> {

    @Autowired
    private lateinit var topic: Topic<AudioSignalRmsInfoKt>

    override fun launchMessage(msg: AudioSignalRmsInfoKt) {
        runBlocking { topic.postMessage(msg) }
    }
}

@Service
class AudioClipLauncherTest : MessageLauncher<AudioClipInfo> {

    @Autowired
    private lateinit var topic: Topic<AudioClipInfo>

    override fun launchMessage(msg: AudioClipInfo) {
        runBlocking { topic.postMessage(msg) }
    }
}
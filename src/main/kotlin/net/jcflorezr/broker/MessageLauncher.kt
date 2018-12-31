package net.jcflorezr.broker

import biz.source_code.dsp.model.AudioSignalKt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.model.InitialConfiguration
import org.springframework.stereotype.Service

interface MessageLauncher<T : Message> {
    fun launchMessage(msg : T)
}

@Service
class SourceFileLauncher : MessageLauncher<InitialConfiguration> {
    override fun launchMessage(msg: InitialConfiguration) {
        GlobalScope.launch { Topic<InitialConfiguration>().postMessage(msg) }
    }
}

@Service
class AudioSignalLauncher : MessageLauncher<AudioSignalKt> {
    override fun launchMessage(msg: AudioSignalKt) {
        GlobalScope.launch { Topic<AudioSignalKt>().postMessage(msg) }
    }
}

@Service
class AudioSignalRmsLauncher : MessageLauncher<AudioSignalRmsInfoKt> {
    override fun launchMessage(msg: AudioSignalRmsInfoKt) {
        GlobalScope.launch { Topic<AudioSignalRmsInfoKt>().postMessage(msg) }
    }
}

@Service
class AudioClipLauncher : MessageLauncher<AudioClipInfo> {
    override fun launchMessage(msg: AudioClipInfo) {
        GlobalScope.launch { Topic<AudioClipInfo>().postMessage(msg) }
    }
}
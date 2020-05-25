package net.jcflorezr.transcriber.audio.splitter.application.di.events

import net.jcflorezr.transcriber.audio.splitter.adapters.events.AudioSplitterKafkaEventDispatcher
import net.jcflorezr.transcriber.core.config.broker.kafka.ComponentTestKafkaStartup

object AudioSplitterKafkaEventDispatcherDI {

    val audioSplitterTestKafkaDispatcher =
        AudioSplitterKafkaEventDispatcher(
            ipAddress = ComponentTestKafkaStartup.ipAddress,
            port = ComponentTestKafkaStartup.port,
            topic = "mono-log",
            acks = 1
        )
}

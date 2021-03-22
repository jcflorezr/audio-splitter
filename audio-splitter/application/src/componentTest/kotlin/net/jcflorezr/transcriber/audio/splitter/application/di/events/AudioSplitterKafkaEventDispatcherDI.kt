package net.jcflorezr.transcriber.audio.splitter.application.di.events

import net.jcflorezr.transcriber.core.config.broker.kafka.ComponentTestKafkaStartup
import net.jcflorezr.transcriber.core.config.broker.kafka.KafkaEventDispatcher

object AudioSplitterKafkaEventDispatcherDI {

    val audioSplitterTestKafkaDispatcher =
        KafkaEventDispatcher(
            ipAddress = ComponentTestKafkaStartup.ipAddress,
            port = ComponentTestKafkaStartup.port,
            topic = "mono-log",
            acks = 1
        )
}

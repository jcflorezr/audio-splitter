package net.jcflorezr.transcriber.audio.splitter.application.di.events

import net.jcflorezr.transcriber.audio.transcriber.adapters.events.AudioTranscriberKafkaEventDispatcher
import net.jcflorezr.transcriber.core.config.broker.kafka.ComponentTestKafkaStartup

object AudioTranscriberKafkaEventDispatcherDI {

    val audioTranscriberTestKafkaDispatcher =
        AudioTranscriberKafkaEventDispatcher(
            ipAddress = ComponentTestKafkaStartup.ipAddress,
            port = ComponentTestKafkaStartup.port,
            topic = "mono-log",
            acks = 1
        )
}

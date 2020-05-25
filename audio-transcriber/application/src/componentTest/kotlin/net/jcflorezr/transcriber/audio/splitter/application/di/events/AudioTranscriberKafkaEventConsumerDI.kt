package net.jcflorezr.transcriber.audio.splitter.application.di.events

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.CoroutineVerticle
import net.jcflorezr.transcriber.audio.transcriber.adapters.events.AudioTranscriberKafkaEventConsumer
import net.jcflorezr.transcriber.core.config.broker.kafka.ComponentTestKafkaStartup

object AudioTranscriberKafkaEventConsumerDI : CoroutineVerticle() {

    override suspend fun start() {
        Vertx.vertx().deployVerticle(audioSplitterKafkaEventConsumerTest())
    }

    private fun audioSplitterKafkaEventConsumerTest() =
        AudioTranscriberKafkaEventConsumer(
            ipAddress = ComponentTestKafkaStartup.ipAddress,
            port = ComponentTestKafkaStartup.port,
            topic = "mono-log",
            groupId = "mono-log-id",
            autoOffsetReset = "earliest",
            enableAutoCommit = true
        )
}

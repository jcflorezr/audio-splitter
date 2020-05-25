package net.jcflorezr.transcriber.audio.splitter.application.di.events

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.CoroutineVerticle
import net.jcflorezr.transcriber.audio.splitter.adapters.events.AudioSplitterKafkaEventConsumer
import net.jcflorezr.transcriber.core.config.broker.kafka.ComponentTestKafkaStartup

object AudioSplitterKafkaEventConsumerDI : CoroutineVerticle() {

    override suspend fun start() {
        Vertx.vertx().deployVerticle(audioSplitterKafkaEventConsumerTest())
    }

    private fun audioSplitterKafkaEventConsumerTest() =
        AudioSplitterKafkaEventConsumer(
            ipAddress = ComponentTestKafkaStartup.ipAddress,
            port = ComponentTestKafkaStartup.port,
            topic = "mono-log",
            groupId = "mono-log-id",
            autoOffsetReset = "earliest",
            enableAutoCommit = true
        )
}

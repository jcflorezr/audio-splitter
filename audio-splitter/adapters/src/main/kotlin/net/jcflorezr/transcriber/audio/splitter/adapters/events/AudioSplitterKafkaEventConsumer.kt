package net.jcflorezr.transcriber.audio.splitter.adapters.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.kafka.client.consumer.KafkaConsumer
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.kafka.client.consumer.subscribeAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.jcflorezr.transcriber.core.domain.EventConsumer
import net.jcflorezr.transcriber.core.domain.EventRouter
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

class AudioSplitterKafkaEventConsumer(
    ipAddress: String,
    port: Int,
    groupId: String,
    autoOffsetReset: String,
    enableAutoCommit: Boolean,
    private val topic: String
) : CoroutineVerticle(), EventConsumer {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val initialConfig = mutableMapOf<String, String>(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "$ipAddress:$port",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
        ConsumerConfig.GROUP_ID_CONFIG to groupId,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to enableAutoCommit.toString()
    )

    override suspend fun start(): Unit = KafkaConsumer.create<String, String>(vertx, initialConfig).run {
        handler { record ->
            launch(Dispatchers.IO) {
                val eventRecord = MAPPER.readTree(record.value())
                EventRouter.route(eventRecord)
            }
            commit()
        }
        subscribeAwait(topic)
    }
}

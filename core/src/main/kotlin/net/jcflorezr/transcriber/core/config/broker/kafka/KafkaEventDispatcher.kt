package net.jcflorezr.transcriber.core.config.broker.kafka

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.producer.KafkaProducerRecord
import io.vertx.kafka.client.serialization.JsonObjectSerializer
import io.vertx.kotlin.kafka.client.producer.writeAwait
import mu.KotlinLogging
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Event
import net.jcflorezr.transcriber.core.domain.EventDispatcher
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer

class KafkaEventDispatcher(
    ipAddress: String,
    port: Int,
    acks: Int,
    private val topic: String
) : EventDispatcher {

    private val logger = KotlinLogging.logger { }
    private val initialConfig = mutableMapOf<String, String>(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "$ipAddress:$port",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonObjectSerializer::class.java.name,
        ProducerConfig.ACKS_CONFIG to acks.toString()
    )
    private val producer = KafkaProducer.create<String, JsonObject>(Vertx.vertx(), initialConfig)
        .also { producer ->
            producer.partitionsFor(topic) { done ->
                done.result().forEach { it.run { logger.info("Partition: id={}, topic={}", partition, topic) } }
            }
        }

    override suspend fun publish(vararg events: Event<AggregateRoot>) {
        events
            .map { event -> EventEnvelope.createNew(event) }
            .map { KafkaProducerRecord.create<String, JsonObject>(topic, JsonObject.mapFrom(it)) }
            .onEach { producer.writeAwait(it) }
    }
}

private data class EventEnvelope private constructor(
    val eventClassName: String,
    val aggregateClassName: String,
    val aggregate: AggregateRoot
) {
    companion object {
        fun createNew(event: Event<AggregateRoot>) = EventEnvelope(
            eventClassName = event.javaClass.name,
            aggregateClassName = event.aggregateRoot.javaClass.name,
            aggregate = event.aggregateRoot
        )
    }
}

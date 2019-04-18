package net.jcflorezr.broker

import mu.KotlinLogging
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.util.JsonUtils
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service
import java.io.File
import java.util.Properties
import javax.annotation.PostConstruct

interface AudioSplitterProducer {
    fun sendAudioClip(audioClip: AudioClipSignal, transactionId: String)
}

@Service
@PropertySource(value = ["classpath:config/kafka.properties"])
class AudioSplitterProducerImpl : AudioSplitterProducer {

    private val logger = KotlinLogging.logger { }
    private val thisClass: Class<AudioSplitterProducerImpl> = this.javaClass
    private lateinit var tempDirectoryPath: String

    @Value("\${kafka-brokers}")
    private lateinit var kafkaBrokers: String
    @Value("\${client-id}")
    private lateinit var clientId: String
    @Value("\${topic-name}")
    private lateinit var topicName: String

    @PostConstruct
    fun init() {
        tempDirectoryPath = thisClass.getResource("/temp-converted-files").path
    }

    override fun sendAudioClip(audioClip: AudioClipSignal, transactionId: String) {
        logger.info { "[$transactionId][7][audio-clip] Sending generated audio clip to message broker (${audioClip.audioClipName})." }
        val messageKey = "${audioClip.audioFileName}_${audioClip.audioClipName}"
        val record = ProducerRecord<String, AudioClipSignal>(topicName, messageKey, audioClip)
        val producer = createProducer()
        kotlin.runCatching {
            val metadata = producer.send(record).get()
            logger.info { "Record sent with key $messageKey to partition ${metadata.partition()} with offset ${metadata.offset()}" }
            if (audioClip.lastClip) {
                File("$tempDirectoryPath/$transactionId").deleteRecursively()
            }
        }.onFailure {
            logger.info { "Error when sending record $it" }
        }
    }

    private fun createProducer(): Producer<String, AudioClipSignal> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaBrokers
        props[ProducerConfig.CLIENT_ID_CONFIG] = clientId.toInt()
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = AudioClipSerializer::class.java.name
        return KafkaProducer<String, AudioClipSignal>(props)
    }

}

class AudioClipSerializer : Serializer<AudioClipSignal> {

    private val logger = KotlinLogging.logger { }

    override fun configure(configs: Map<String, *>, isKey: Boolean) {}

    override fun serialize(topic: String, data: AudioClipSignal): ByteArray? {
        var retVal: ByteArray? = null
        val objectMapper = JsonUtils.MAPPER
        try {
            retVal = objectMapper.writeValueAsString(data).toByteArray()
        } catch (exception: Exception) {
            logger.error { "Error in serializing object $data" }
        }

        return retVal
    }

    override fun close() {}
}
package net.jcflorezr.transcriber.core.config.broker.kafka

import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory
import mu.KotlinLogging
import org.testcontainers.containers.KafkaContainer

object ComponentTestKafkaStartup {

    private val logger = KotlinLogging.logger { }

    private const val EXPECTED_EXISTING_KAFKA_CONTAINER_NAME = "/transcriber-component-test-kafka"
    private const val EXPECTED_EXISTING_ZOOKEEPER_CONTAINER_NAME = "/transcriber-component-test-zookeeper"

    private val ipAddressAndPortTuple = getKafkaContainer() ?: createNewKafkaContainer()
    val ipAddress = ipAddressAndPortTuple.first
    val port = ipAddressAndPortTuple.second

    private fun getKafkaContainer(): Pair<String, Int>? {
        val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
        val dockerClient = DockerClientImpl.getInstance(dockerConfig)
            .withDockerCmdExecFactory(OkHttpDockerCmdExecFactory())
        return dockerClient.listContainersCmd().exec()
            .isZookeeperPresent()
            ?.isKafkaPresent()
            ?.also {
                logger.info { "==== The expected existing container '$EXPECTED_EXISTING_ZOOKEEPER_CONTAINER_NAME' was found. " +
                    "No need to create a new one to run the component tests :) ====" }
                logger.info { "==== The expected existing container '$EXPECTED_EXISTING_KAFKA_CONTAINER_NAME' was found. " +
                    "No need to create a new one to run the component tests :) ====" }
            }
            ?.run { "localhost" to 29092 } // confluent kafka container is created with network = host
    }

    private fun List<com.github.dockerjava.api.model.Container>.isZookeeperPresent() =
        find { it.names.contains(EXPECTED_EXISTING_ZOOKEEPER_CONTAINER_NAME) }
        ?.let { this }

    private fun List<com.github.dockerjava.api.model.Container>.isKafkaPresent() =
        find { it.names.contains(EXPECTED_EXISTING_KAFKA_CONTAINER_NAME) }

    private fun createNewKafkaContainer(): Pair<String, Int> =
        KafkaContainer("5.5.0")
            .also { it.start() }
            .run { containerIpAddress to firstMappedPort }
}

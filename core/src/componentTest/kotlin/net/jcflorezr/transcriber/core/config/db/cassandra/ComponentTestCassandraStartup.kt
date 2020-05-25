package net.jcflorezr.transcriber.core.config.db.cassandra

import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory
import mu.KotlinLogging
import org.testcontainers.containers.CassandraContainer

abstract class ComponentTestCassandraStartup {

    private val logger = KotlinLogging.logger { }

    companion object {
        private const val EXPECTED_EXISTING_CONTAINER_NAME = "/transcriber-component-test-cassandra"
    }

    private val ipAddressAndPortTuple = getCassandraContainer() ?: createNewCassandraContainer()
    protected val dbIpAddress = ipAddressAndPortTuple.first
    protected val dbPort = ipAddressAndPortTuple.second

    private fun getCassandraContainer(): Pair<String, Int>? {
        val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
        val dockerClient = DockerClientImpl.getInstance(dockerConfig)
            .withDockerCmdExecFactory(OkHttpDockerCmdExecFactory())
        return dockerClient.listContainersCmd().exec().asSequence()
            .find { it.names.contains(EXPECTED_EXISTING_CONTAINER_NAME) }
            ?.ports
            ?.firstOrNull { it.publicPort?.equals(19043) ?: false }
            ?.run { (ip ?: "localhost") to (publicPort ?: 9042) }
            ?.also {
                logger.info { "==== The expected existing container '$EXPECTED_EXISTING_CONTAINER_NAME' was found. " +
                    "No need to create a new one to run the component tests :) ====" }
            }
    }

    private fun createNewCassandraContainer(): Pair<String, Int> =
        TestCassandraContainer("cassandra:3.11.2")
            .also { it.start() }
            .run { containerIpAddress to firstMappedPort }
}

class TestCassandraContainer(
    imageName: String
) : CassandraContainer<TestCassandraContainer>(imageName)

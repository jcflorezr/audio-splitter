package net.jcflorezr.transcriber.core.config.db.cassandra

import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration
import org.springframework.data.cassandra.config.SchemaAction
import org.testcontainers.containers.CassandraContainer

abstract class IntegrationTestCassandraConfig : AbstractCassandraConfiguration() {

    companion object {

        private const val EXPECTED_EXISTING_CONTAINER_NAME = "/transcriber-test-cassandra"

        private val portAndIpAddressTuple = getCassandraContainer() ?: createNewCassandraContainer()

        private fun getCassandraContainer(): Pair<String, Int>? {
            val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
            val dockerClient = DockerClientImpl.getInstance(dockerConfig)
                .withDockerCmdExecFactory(OkHttpDockerCmdExecFactory())
            return dockerClient.listContainersCmd().exec().asSequence()
                .find { it.names.contains(EXPECTED_EXISTING_CONTAINER_NAME) }
                ?.let {
                    Pair(it.networkSettings?.networks?.get("bridge")?.ipAddress ?: "localhost",
                        it.ports.first().publicPort ?: 9042)
                }
        }

        private fun createNewCassandraContainer(): Pair<String, Int> =
            TestCassandraContainer("cassandra:3.11.2")
                .also { it.start() }
                .run { containerIpAddress to firstMappedPort }
    }

    override fun getContactPoints(): String = portAndIpAddressTuple.first

    override fun getPort(): Int = portAndIpAddressTuple.second

    override fun getSchemaAction(): SchemaAction = SchemaAction.CREATE_IF_NOT_EXISTS
}

class TestCassandraContainerV(
    imageName: String
) : CassandraContainer<TestCassandraContainer>(imageName)

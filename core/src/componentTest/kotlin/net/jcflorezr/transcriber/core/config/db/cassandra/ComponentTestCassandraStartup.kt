package net.jcflorezr.transcriber.core.config.db.cassandra

import com.datastax.driver.core.Cluster
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory
import io.vertx.cassandra.CassandraClient
import io.vertx.cassandra.CassandraClientOptions
import io.vertx.core.Vertx
import java.io.File
import java.nio.file.Files
import mu.KotlinLogging
import org.testcontainers.containers.CassandraContainer

abstract class ComponentTestCassandraStartup(
    private val keyspaceName: String,
    private val startUpScriptName: String
) {

    private val logger = KotlinLogging.logger { }

    companion object {
        private const val EXPECTED_EXISTING_CONTAINER_NAME = "/transcriber-component-test-cassandra"
        private const val IP_ADDRESS_ENV_NAME = "TRANSCRIBER_COMPONENT_CASSANDRA_DB_IP_ADDRESS"
        private const val PORT_ENV_NAME = "TRANSCRIBER_COMPONENT_CASSANDRA_DB_PORT"
    }

    private val ipAddressAndPortTuple = getCassandraContainer() ?: createNewCassandraContainer()
    private val dbIpAddress = ipAddressAndPortTuple.first
    private val dbPort = ipAddressAndPortTuple.second
    private val cluster: Cluster = Cluster.builder().addContactPoint(dbIpAddress).withPort(dbPort).build()
    private val startupScriptFilePath = this.javaClass.getResource("/cassandra").path

    init {
        System.setProperty(IP_ADDRESS_ENV_NAME, dbIpAddress)
        System.setProperty(PORT_ENV_NAME, dbPort.toString())
        executeStartupScript()
    }

    private fun getCassandraContainer(): Pair<String, Int>? {
        val dockerConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
        val dockerClient = DockerClientImpl.getInstance(dockerConfig)
            .withDockerCmdExecFactory(OkHttpDockerCmdExecFactory())
        return dockerClient.listContainersCmd().exec()
            .find { it.names.contains(EXPECTED_EXISTING_CONTAINER_NAME) }
            ?.ports
            ?.firstOrNull { it.publicPort?.equals(19043) ?: false }
            ?.run { (ip ?: "localhost") to (publicPort ?: 9042) }
            ?.also {
                logger.info {
                    "==== The expected existing container '$EXPECTED_EXISTING_CONTAINER_NAME' was found. " +
                        "No need to create a new one to run the component tests :) ===="
                }
            }
    }

    private fun createNewCassandraContainer(): Pair<String, Int> =
        TestCassandraContainer("cassandra:3.11.2")
            .also { it.start() }
            .run { containerIpAddress to firstMappedPort }

    private fun executeStartupScript() {
        Files.readAllLines(
            File("$startupScriptFilePath/$startUpScriptName.cql").toPath(), Charsets.UTF_8
        )
            .onEach { statementToExecute ->
                /*
                    The startup script to create the keyspace have to be run with Cassandra's Java Driver
                    as the entire configuration must be ready before deploying vert.x Verticles
                 */
                logger.info { "Executing database startup script: $statementToExecute" }
                val session = cluster.connect()
                session.execute(statementToExecute)
            }
    }

    private val cassandraSession = cluster.connect(keyspaceName)
    fun cassandraSession() = cassandraSession

    private val cassandraClient: CassandraClient
        get() = CassandraClientOptions()
            .addContactPoint(dbIpAddress)
            .setPort(dbPort)
            .setKeyspace(keyspaceName)
            .let { options -> CassandraClient.create(Vertx.vertx(), options) }

    fun cassandraClient(): CassandraClient = cassandraClient
}

class TestCassandraContainer(
    imageName: String
) : CassandraContainer<TestCassandraContainer>(imageName)

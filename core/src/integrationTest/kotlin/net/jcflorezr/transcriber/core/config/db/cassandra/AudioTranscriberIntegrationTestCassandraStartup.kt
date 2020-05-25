package net.jcflorezr.transcriber.core.config.db.cassandra

import com.datastax.driver.core.Cluster
import java.io.File
import java.nio.file.Files
import mu.KotlinLogging

object AudioTranscriberIntegrationTestCassandraStartup : IntegrationTestCassandraStartup() {

    private val logger = KotlinLogging.logger { }

    const val KEYSPACE_NAME = "AUDIO_TRANSCRIBER"

    private val thisClass: Class<AudioTranscriberIntegrationTestCassandraStartup> =
        AudioTranscriberIntegrationTestCassandraStartup::class.java

    init {
        val startupScriptFilePath = thisClass.getResource("/cassandra").path
        executeStartupScript(startupScriptFilePath)
    }

    private fun executeStartupScript(startupScriptFilePath: String) {
        Files.readAllLines(
            File("$startupScriptFilePath/audio-transcriber-database-init-script.cql").toPath(), Charsets.UTF_8)
        .onEach { statementToExecute ->
            /*
                The startup script to create the keyspace have to be run with Cassandra's Java Driver
                as the entire configuration must be ready before deploying vert.x Verticles
             */
            logger.info { "Executing database startup script: $statementToExecute" }
            val cluster = Cluster.builder().addContactPoint(dbIpAddress).withPort(dbPort).build()
            val session = cluster.connect()
            session.execute(statementToExecute)
        }
    }

    fun dbIpAddress() = dbIpAddress

    fun dbPort() = dbPort
}

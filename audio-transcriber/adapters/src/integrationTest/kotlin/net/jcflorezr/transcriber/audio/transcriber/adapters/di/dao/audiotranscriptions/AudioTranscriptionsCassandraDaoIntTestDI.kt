package net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.audiotranscriptions

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.DataType
import com.datastax.driver.core.Session
import com.datastax.driver.core.schemabuilder.SchemaBuilder
import io.vertx.cassandra.CassandraClient
import io.vertx.cassandra.CassandraClientOptions
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import mu.KotlinLogging
import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.AlternativeCassandraRecord
import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.AudioTranscriptionsCassandraDao
import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.TranscriptionCassandraRecord
import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.WordCassandraRecord
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberIntegrationTestCassandraStartup

object AudioTranscriptionsCassandraDaoIntTestDI : CoroutineVerticle() {

    val cassandraClient: CassandraClient
    val audioTranscriptionsCassandraDao: AudioTranscriptionsCassandraDao

    init {
        val transcriberIntegrationStartup = AudioTranscriberIntegrationTestCassandraStartup
        cassandraClient = CassandraClientOptions()
            .addContactPoint(transcriberIntegrationStartup.dbIpAddress())
            .setPort(transcriberIntegrationStartup.dbPort())
            .setKeyspace(AudioTranscriberIntegrationTestCassandraStartup.KEYSPACE_NAME)
            .let { options -> CassandraClient.create(Vertx.vertx(), options) }
        audioTranscriptionsCassandraDao = AudioTranscriptionsCassandraDao(cassandraClient)
        AudioTranscriptionsTables.createTablesInDb(transcriberIntegrationStartup)
    }

    override suspend fun start() {
        vertx.deployVerticleAwait(audioTranscriptionsCassandraDao)
    }
}

private class AudioTranscriptionsTables(private val cassandraSession: Session) {

    private val logger = KotlinLogging.logger { }

    companion object {
        fun createTablesInDb(transcriberIntegrationConfig: AudioTranscriberIntegrationTestCassandraStartup) {
            /*
                The statements for creating tables have to be run with Cassandra's Java Driver
                as the entire configuration must be ready before deploying the vert.x Verticles
             */
            val cassandraSession = transcriberIntegrationConfig.run {
                Cluster.builder().addContactPoint(dbIpAddress()).withPort(dbPort()).build()
                    .connect(KEYSPACE_NAME)
            }
            val audioTranscriptionsTables = AudioTranscriptionsTables(cassandraSession)
            audioTranscriptionsTables.createTranscriptionsTable()
            audioTranscriptionsTables.createAlternativesTable()
            audioTranscriptionsTables.createWordsTable()
        }
    }

    private fun createTranscriptionsTable(): Unit = TranscriptionCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(HOURS_COLUMN, DataType.cint())
            .addClusteringColumn(MINUTES_COLUMN, DataType.cint())
            .addClusteringColumn(SECONDS_COLUMN, DataType.cint())
            .addClusteringColumn(TENTHS_COLUMN, DataType.cint())
    }.let { createTableSentence ->
        logger.info { "Creating ${TranscriptionCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }

    private fun createAlternativesTable(): Unit = AlternativeCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(HOURS_COLUMN, DataType.cint())
            .addClusteringColumn(MINUTES_COLUMN, DataType.cint())
            .addClusteringColumn(SECONDS_COLUMN, DataType.cint())
            .addClusteringColumn(TENTHS_COLUMN, DataType.cint())
            .addClusteringColumn(ALTERNATIVE_POSITION_COLUMN, DataType.cint())
            .addColumn(TRANSCRIPTION_COLUMN, DataType.text())
            .addColumn(CONFIDENCE_COLUMN, DataType.cfloat())
    }.let { createTableSentence ->
        logger.info { "Creating ${AlternativeCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }

    private fun createWordsTable(): Unit = WordCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(HOURS_COLUMN, DataType.cint())
            .addClusteringColumn(MINUTES_COLUMN, DataType.cint())
            .addClusteringColumn(SECONDS_COLUMN, DataType.cint())
            .addClusteringColumn(TENTHS_COLUMN, DataType.cint())
            .addClusteringColumn(ALTERNATIVE_POSITION_COLUMN, DataType.cint())
            .addClusteringColumn(ALTERNATIVE_WORD_POSITION_COLUMN, DataType.cint())
            .addColumn(WORD_COLUMN, DataType.text())
            .addColumn(FROM_TIME_COLUMN, DataType.cfloat())
            .addColumn(TO_TIME_COLUMN, DataType.cfloat())
    }.let { createTableSentence ->
        logger.info { "Creating ${WordCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }
}

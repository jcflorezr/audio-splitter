package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audioclips

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
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips.ActiveSegmentCassandraRecord
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips.AudioClipCassandraRecord
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips.AudioClipsCassandraDao
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterIntegrationTestCassandraStartup

object AudioClipsCassandraDaoIntTestDI : CoroutineVerticle() {

    val cassandraClient: CassandraClient
    val audioClipsCassandraDao: AudioClipsCassandraDao

    init {
        val splitterIntegrationStartup = AudioSplitterIntegrationTestCassandraStartup
        cassandraClient = CassandraClientOptions()
            .addContactPoint(splitterIntegrationStartup.dbIpAddress())
            .setPort(splitterIntegrationStartup.dbPort())
            .setKeyspace(AudioSplitterIntegrationTestCassandraStartup.KEYSPACE_NAME)
            .let { options -> CassandraClient.create(Vertx.vertx(), options) }
        audioClipsCassandraDao = AudioClipsCassandraDao(cassandraClient)
        AudioClipsTables.createTablesInDb(splitterIntegrationStartup)
    }

    override suspend fun start() {
        vertx.deployVerticleAwait(audioClipsCassandraDao)
    }
}

private class AudioClipsTables(private val cassandraSession: Session) {

    private val logger = KotlinLogging.logger { }

    companion object {
        fun createTablesInDb(splitterIntegrationConfig: AudioSplitterIntegrationTestCassandraStartup) {
            /*
                The statements for creating tables have to be run with Cassandra's Java Driver
                as the entire configuration must be ready before deploying the vert.x Verticles
             */
            val cassandraSession = splitterIntegrationConfig.run {
                Cluster.builder().addContactPoint(dbIpAddress()).withPort(dbPort()).build()
                    .connect(KEYSPACE_NAME)
            }
            val audioClipsTables = AudioClipsTables(cassandraSession)
            audioClipsTables.createAudioClipsTable()
            audioClipsTables.createActiveSegmentsTable()
        }
    }

    private fun createAudioClipsTable(): Unit = AudioClipCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(HOURS_COLUMN, DataType.cint())
            .addClusteringColumn(MINUTES_COLUMN, DataType.cint())
            .addClusteringColumn(SECONDS_COLUMN, DataType.cint())
            .addClusteringColumn(TENTHS_COLUMN, DataType.cint())
            .addColumn(DURATION_COLUMN, DataType.cfloat())
            .addColumn(CLIP_FILE_NAME_COLUMN, DataType.text())
    }.let { createTableSentence ->
        logger.info { "Creating ${AudioClipCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }

    private fun createActiveSegmentsTable(): Unit = ActiveSegmentCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(HOURS_COLUMN, DataType.cint())
            .addClusteringColumn(MINUTES_COLUMN, DataType.cint())
            .addClusteringColumn(SECONDS_COLUMN, DataType.cint())
            .addClusteringColumn(TENTHS_COLUMN, DataType.cint())
            .addClusteringColumn(SEGMENT_START_IN_SECONDS_COLUMN, DataType.cfloat())
            .addColumn(SEGMENT_END_IN_SECONDS_COLUMN, DataType.cfloat())
            .addColumn(DURATION_COLUMN, DataType.cfloat())
            .addColumn(SEGMENT_HOURS_COLUMN, DataType.cint())
            .addColumn(SEGMENT_MINUTES_COLUMN, DataType.cint())
            .addColumn(SEGMENT_SECONDS_COLUMN, DataType.cint())
            .addColumn(SEGMENT_TENTHS_COLUMN, DataType.cint())
    }.let { createTableSentence ->
        logger.info { "Creating ${ActiveSegmentCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }
}

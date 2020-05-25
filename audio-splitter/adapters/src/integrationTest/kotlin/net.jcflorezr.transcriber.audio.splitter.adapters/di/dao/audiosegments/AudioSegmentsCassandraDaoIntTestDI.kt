package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments

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
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.AudioSegmentCassandraRecord
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.AudioSegmentsCassandraDao
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.BasicAudioSegmentsCassandraDao
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterIntegrationTestCassandraStartup

object AudioSegmentsCassandraDaoIntTestDI : CoroutineVerticle() {

    val cassandraClient: CassandraClient
    val audioSegmentsCassandraDao: AudioSegmentsCassandraDao
    val basicAudioSegmentsCassandraDao: BasicAudioSegmentsCassandraDao

    init {
        val splitterIntegrationStartup = AudioSplitterIntegrationTestCassandraStartup
        cassandraClient = CassandraClientOptions()
            .addContactPoint(splitterIntegrationStartup.dbIpAddress())
            .setPort(splitterIntegrationStartup.dbPort())
            .setKeyspace(AudioSplitterIntegrationTestCassandraStartup.KEYSPACE_NAME)
            .let { options -> CassandraClient.create(Vertx.vertx(), options) }
        audioSegmentsCassandraDao = AudioSegmentsCassandraDao(cassandraClient)
        basicAudioSegmentsCassandraDao = BasicAudioSegmentsCassandraDao(cassandraClient)
        AudioSegmentsTables.createTablesInDb(splitterIntegrationStartup)
    }

    override suspend fun start() {
        vertx.deployVerticleAwait(audioSegmentsCassandraDao)
        vertx.deployVerticleAwait(basicAudioSegmentsCassandraDao)
    }
}

private class AudioSegmentsTables(private val cassandraSession: Session) {

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
            val audioSegmentsTables = AudioSegmentsTables(cassandraSession)
            audioSegmentsTables.createAudioSegmentsTable()
        }
    }

    private fun createAudioSegmentsTable(): Unit = AudioSegmentCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(SEGMENT_START_IN_SECONDS_COLUMN, DataType.cfloat())
            .addClusteringColumn(SEGMENT_END_IN_SECONDS_COLUMN, DataType.cfloat())
            .addClusteringColumn(SEGMENT_START_COLUMN, DataType.cint())
            .addClusteringColumn(SEGMENT_END_COLUMN, DataType.cint())
            .addColumn(SEGMENT_RMS_COLUMN, DataType.cdouble())
            .addColumn(SEGMENT_BYTES_COLUMN, DataType.blob())
    }.let { createTableSentence ->
        logger.info { "Creating ${AudioSegmentCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }
}

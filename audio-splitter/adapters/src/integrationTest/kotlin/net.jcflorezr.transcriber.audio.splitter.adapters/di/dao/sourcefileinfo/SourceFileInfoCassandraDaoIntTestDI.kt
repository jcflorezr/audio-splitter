package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.sourcefileinfo

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
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo.SourceFileContentInfoCassandraRecord
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo.SourceFileInfoCassandraDao
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo.SourceFileMetadataCassandraRecord
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterIntegrationTestCassandraStartup

object SourceFileInfoCassandraDaoIntTestDI : CoroutineVerticle() {

    val cassandraClient: CassandraClient
    val sourceFileInfoCassandraDao: SourceFileInfoCassandraDao

    init {
        val splitterIntegrationStartup = AudioSplitterIntegrationTestCassandraStartup
        cassandraClient = CassandraClientOptions()
            .addContactPoint(splitterIntegrationStartup.dbIpAddress())
            .setPort(splitterIntegrationStartup.dbPort())
            .setKeyspace(AudioSplitterIntegrationTestCassandraStartup.KEYSPACE_NAME)
            .let { options -> CassandraClient.create(Vertx.vertx(), options) }
        sourceFileInfoCassandraDao = SourceFileInfoCassandraDao(cassandraClient)
        SourceFileInfoTables.createTablesInDb(splitterIntegrationStartup)
    }

    override suspend fun start() {
        vertx.deployVerticleAwait(sourceFileInfoCassandraDao)
    }
}

private class SourceFileInfoTables(private val cassandraSession: Session) {

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
            val sourceFileInfoTables = SourceFileInfoTables(cassandraSession)
            sourceFileInfoTables.createSourceFileMetadataTable()
            sourceFileInfoTables.createSourceFileContentInfoTable()
        }
    }

    private fun createSourceFileMetadataTable(): Unit = SourceFileMetadataCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addColumn(TITLE_COLUMN, DataType.text())
            .addColumn(ALBUM_COLUMN, DataType.text())
            .addColumn(ARTIST_COLUMN, DataType.text())
            .addColumn(TRACK_NUMBER_COLUMN, DataType.text())
            .addColumn(GENRE_COLUMN, DataType.text())
            .addColumn(DURATION_COLUMN, DataType.cint())
            .addColumn(SAMPLE_RATE_COLUMN, DataType.text())
            .addColumn(CHANNELS_COLUMN, DataType.text())
            .addColumn(COMMENTS_COLUMN, DataType.text())
    }.let { createTableSentence ->
        logger.info { "Creating ${SourceFileMetadataCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }

    private fun createSourceFileContentInfoTable(): Unit = SourceFileContentInfoCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addColumn(CHANNELS_COLUMN, DataType.cint())
            .addColumn(SAMPLE_RATE_COLUMN, DataType.cint())
            .addColumn(SAMPLE_SIZE_IN_BITS_COLUMN, DataType.cint())
            .addColumn(FRAME_SIZE_COLUMN, DataType.cint())
            .addColumn(SAMPLE_SIZE_COLUMN, DataType.cint())
            .addColumn(BIG_ENDIAN_COLUMN, DataType.cboolean())
            .addColumn(ENCODING_COLUMN, DataType.text())
            .addColumn(TOTAL_FRAMES_COLUMN, DataType.cint())
            .addColumn(EXACT_TOTAL_FRAMES_COLUMN, DataType.cint())
            .addColumn(TOTAL_FRAMES_BY_SECOND_COLUMN, DataType.cint())
            .addColumn(REMAINING_FRAMES_COLUMN, DataType.cint())
            .addColumn(FRAMES_PER_SECOND_COLUMN, DataType.cint())
            .addColumn(NUM_OF_AUDIO_SEGMENTS_COLUMN, DataType.cint())
            .addColumn(AUDIO_SEGMENT_LENGTH_COLUMN, DataType.cint())
            .addColumn(AUDIO_SEGMENT_LENGTH_IN_BYTES_COLUMN, DataType.cint())
            .addColumn(AUDIO_SEGMENTS_PER_SECOND_COLUMN, DataType.cint())
            .addColumn(REMAINING_AUDIO_SEGMENTS_COLUMN, DataType.cint())
    }.let { createTableSentence ->
        logger.info { "Creating ${SourceFileContentInfoCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }
}

package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments

import com.datastax.driver.core.DataType
import com.datastax.driver.core.Session
import com.datastax.driver.core.schemabuilder.SchemaBuilder
import mu.KotlinLogging
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.AudioSegmentCassandraRecord
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterIntegrationTestCassandraStartup

class AudioSegmentsTablesCreation(private val cassandraSession: Session) {

    private val logger = KotlinLogging.logger { }

    companion object {
        fun createTablesInDb(cassandraStartup: AudioSplitterIntegrationTestCassandraStartup) {
            val audioSegmentsTables = AudioSegmentsTablesCreation(cassandraStartup.cassandraSession())
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

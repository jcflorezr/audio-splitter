package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audioclips

import com.datastax.driver.core.DataType
import com.datastax.driver.core.Session
import com.datastax.driver.core.schemabuilder.SchemaBuilder
import mu.KotlinLogging
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips.ActiveSegmentCassandraRecord
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips.AudioClipCassandraRecord
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterComponentTestCassandraStartup

class AudioClipsTablesCreation(private val cassandraSession: Session) {

    private val logger = KotlinLogging.logger { }

    companion object {
        fun createTablesInDb(cassandraStartup: AudioSplitterComponentTestCassandraStartup) {
            val audioClipsTables = AudioClipsTablesCreation(cassandraStartup.cassandraSession())
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

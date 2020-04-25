package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import com.datastax.driver.core.querybuilder.QueryBuilder
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.select

class BasicAudioSegmentsCassandraDao(
    private val cassandraTemplate: CassandraOperations
) {

    fun findBy(audioFileName: String): Sequence<BasicAudioSegmentCassandraRecord> =
        QueryBuilder
            .select()
                .column(BasicAudioSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN)
                .column(BasicAudioSegmentCassandraRecord.SEGMENT_START_IN_SECONDS_COLUMN)
                .column(BasicAudioSegmentCassandraRecord.SEGMENT_END_IN_SECONDS_COLUMN)
                .column(BasicAudioSegmentCassandraRecord.SEGMENT_START_COLUMN)
                .column(BasicAudioSegmentCassandraRecord.SEGMENT_END_COLUMN)
                .column(BasicAudioSegmentCassandraRecord.AUDIO_SEGMENT_RMS_COLUMN)
            .from(BasicAudioSegmentCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(BasicAudioSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN, audioFileName))
            .let { query -> cassandraTemplate.select<BasicAudioSegmentCassandraRecord>(query).asSequence() }
}
package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.Select
import io.vertx.cassandra.CassandraClient
import io.vertx.kotlin.cassandra.executeWithFullFetchAwait
import io.vertx.kotlin.cassandra.prepareAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment

class BasicAudioSegmentsCassandraDao(private val cassandraClient: CassandraClient) : CoroutineVerticle() {

    private val basicAudioSegmentsQueries = BasicAudioSegmentsQueries()
    private val preparedStatements = mutableMapOf<String, PreparedStatement>()

    private val findOrStorePreparedStatement: ((String) -> Deferred<PreparedStatement>) = { statement ->
        async { preparedStatements.getOrPut(statement) { cassandraClient.prepareAwait(statement) } }
    }

    override suspend fun start() {
        basicAudioSegmentsQueries.getMultipleBasicAudioSegmentsQuery.toString().let(findOrStorePreparedStatement).await()
    }

    suspend fun findBy(audioFileName: String): List<BasicAudioSegmentCassandraRecord> =
        basicAudioSegmentsQueries.getMultipleBasicAudioSegmentsQuery.toString()
            .let(findOrStorePreparedStatement).await()
            .bind(audioFileName)
            .let { basicAudioSegmentsStatement ->
                cassandraClient.executeWithFullFetchAwait(basicAudioSegmentsStatement)
                    .map { row -> BasicAudioSegmentCassandraRecord.fromCassandraRow(row) }
            }

    fun toRecord(audioSegment: BasicAudioSegment) = BasicAudioSegmentCassandraRecord.fromEntity(audioSegment)
}

class BasicAudioSegmentsQueries {

    companion object {
        private val QUESTION_MARK = QueryBuilder.bindMarker()
    }

    val getMultipleBasicAudioSegmentsQuery: Select.Where =
        QueryBuilder
            .select()
            .column(BasicAudioSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN)
            .column(BasicAudioSegmentCassandraRecord.SEGMENT_START_IN_SECONDS_COLUMN)
            .column(BasicAudioSegmentCassandraRecord.SEGMENT_END_IN_SECONDS_COLUMN)
            .column(BasicAudioSegmentCassandraRecord.SEGMENT_START_COLUMN)
            .column(BasicAudioSegmentCassandraRecord.SEGMENT_END_COLUMN)
            .column(BasicAudioSegmentCassandraRecord.AUDIO_SEGMENT_RMS_COLUMN)
            .from(BasicAudioSegmentCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(BasicAudioSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN, QUESTION_MARK))
}

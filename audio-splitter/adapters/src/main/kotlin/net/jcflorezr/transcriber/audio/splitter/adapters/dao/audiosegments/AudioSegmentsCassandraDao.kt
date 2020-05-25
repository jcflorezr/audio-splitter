package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.Select
import io.vertx.cassandra.CassandraClient
import io.vertx.cassandra.Mapper
import io.vertx.cassandra.MappingManager
import io.vertx.kotlin.cassandra.executeWithFullFetchAwait
import io.vertx.kotlin.cassandra.prepareAwait
import io.vertx.kotlin.cassandra.saveAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment

class AudioSegmentsCassandraDao(private val cassandraClient: CassandraClient) : CoroutineVerticle() {

    private val audioSegmentsQueries = AudioSegmentsQueries()
    private val preparedStatements = mutableMapOf<String, PreparedStatement>()

    private val cassandraMapper = MappingManager.create(cassandraClient)
    private val audioSegmentRecordTemplate: Mapper<AudioSegmentCassandraRecord> =
        cassandraMapper.mapper(AudioSegmentCassandraRecord::class.java)

    private val findOrStorePreparedStatement: ((String) -> Deferred<PreparedStatement>) = { statement ->
        async { preparedStatements.getOrPut(statement) { cassandraClient.prepareAwait(statement) } }
    }

    override suspend fun start() {
        audioSegmentsQueries.getMultipleAudioSegmentsByRangeQuery.toString().let(findOrStorePreparedStatement).await()
    }

    suspend fun save(audioSegmentCassandraRecord: AudioSegmentCassandraRecord) = audioSegmentCassandraRecord.run {
        audioSegmentRecordTemplate.saveAwait(audioSegmentCassandraRecord)
    }

    suspend fun findRange(
        audioFileName: String,
        segmentStartInSeconds: Float,
        segmentEndInSeconds: Float
    ): List<AudioSegmentCassandraRecord> =
        audioSegmentsQueries.getMultipleAudioSegmentsByRangeQuery.toString()
            .let(findOrStorePreparedStatement).await()
            .bind(audioFileName, segmentStartInSeconds, segmentEndInSeconds)
            .let { audioSegmentsStatement ->
                cassandraClient.executeWithFullFetchAwait(audioSegmentsStatement)
                    .map { row -> AudioSegmentCassandraRecord.fromCassandraRow(row) }
            }

    fun toRecord(audioSegment: AudioSegment) = AudioSegmentCassandraRecord.fromEntity(audioSegment)
}

class AudioSegmentsQueries {

    companion object {
        private val QUESTION_MARK = QueryBuilder.bindMarker()
    }

    val getMultipleAudioSegmentsByRangeQuery: Select.Where =
        QueryBuilder
            .select()
            .from(AudioSegmentCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(AudioSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN, QUESTION_MARK))
            .and(QueryBuilder.gte(AudioSegmentCassandraRecord.SEGMENT_START_IN_SECONDS_COLUMN, QUESTION_MARK))
            .and(QueryBuilder.lte(AudioSegmentCassandraRecord.SEGMENT_START_IN_SECONDS_COLUMN, QUESTION_MARK))
}

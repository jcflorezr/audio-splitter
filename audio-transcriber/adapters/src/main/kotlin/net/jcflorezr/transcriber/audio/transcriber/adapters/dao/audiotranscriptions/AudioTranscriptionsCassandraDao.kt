package net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions

import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.Select
import io.vertx.cassandra.CassandraClient
import io.vertx.cassandra.Mapper
import io.vertx.cassandra.MappingManager
import io.vertx.core.AsyncResult
import io.vertx.kotlin.cassandra.executeWithFullFetchAwait
import io.vertx.kotlin.cassandra.prepareAwait
import io.vertx.kotlin.cassandra.saveAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.core.exception.PersistenceException

class AudioTranscriptionsCassandraDao(private val cassandraClient: CassandraClient) : CoroutineVerticle() {

    private val transcriptionsQueries = TranscriptionsQueries()
    private val preparedStatements = mutableMapOf<String, PreparedStatement>()

    private val cassandraMapper = MappingManager.create(cassandraClient)
    private val transcriptionRecordTemplate: Mapper<TranscriptionCassandraRecord> =
        cassandraMapper.mapper(TranscriptionCassandraRecord::class.java)
    private val alternativeRecordTemplate: Mapper<AlternativeCassandraRecord> =
        cassandraMapper.mapper(AlternativeCassandraRecord::class.java)
    private val wordRecordTemplate: Mapper<WordCassandraRecord> =
        cassandraMapper.mapper(WordCassandraRecord::class.java)

    private val recordErrorHandler: ((AsyncResult<Void>) -> Unit) = { result ->
        if (result.failed()) { throw PersistenceException.recordNotSavedInRepository(result.cause()) }
    }

    private val findOrStorePreparedStatement: ((String) -> Deferred<PreparedStatement>) = { statement ->
        async { preparedStatements.getOrPut(statement) { cassandraClient.prepareAwait(statement) } }
    }

    override suspend fun start() {
        transcriptionsQueries.getMultipleTranscriptionsQuery.toString().let(findOrStorePreparedStatement).await()
        transcriptionsQueries.getTranscriptionAlternativesQuery.toString().let(findOrStorePreparedStatement).await()
        transcriptionsQueries.getAlternativeWordsQuery.toString().let(findOrStorePreparedStatement).await()
    }

    suspend fun save(audioTranscriptionCassandraRecord: AudioTranscriptionCassandraRecord) = audioTranscriptionCassandraRecord.run {
        transcriptionRecordTemplate.saveAwait(transcriptionCassandraRecord)
        alternativesCassandraRecord.forEach { alternativeRecordTemplate.saveAwait(it) }
        wordsCassandraRecord.forEach { wordRecordTemplate.saveAwait(it) }
    }

    suspend fun findBy(audioFileName: String): List<AudioTranscriptionCassandraRecord> =
        transcriptionsQueries.getMultipleTranscriptionsQuery.toString()
            .let(findOrStorePreparedStatement).await()
            .bind(audioFileName)
            .let { transcriptionsStatement ->
                cassandraClient.executeWithFullFetchAwait(transcriptionsStatement)
                    .map { row -> TranscriptionCassandraRecord.fromCassandraRow(row) }
                    .map { transcriptionRecord -> transcriptionRecord to transcriptionRecord.getTranscriptionAlternatives() }
                    .map { tuple -> tuple to tuple.second.map { alternativeRecord -> alternativeRecord.getAlternativeWords() } }
                    .map { tuple ->
                        AudioTranscriptionCassandraRecord(
                            transcriptionCassandraRecord = tuple.first.first,
                            alternativesCassandraRecord = tuple.first.second,
                            wordsCassandraRecord = tuple.second.flatten()
                        )
                    }
            }

    private suspend fun TranscriptionCassandraRecord.getTranscriptionAlternatives() =
        transcriptionsQueries.getTranscriptionAlternativesQuery.toString()
            .let(findOrStorePreparedStatement).await()
            .bind(audioFileName, hours, minutes, seconds, tenthsOfSecond)
            .let { alternativesStatement -> cassandraClient.executeWithFullFetchAwait(alternativesStatement) }
            .map { row -> AlternativeCassandraRecord.fromCassandraRow(row) }

    private suspend fun AlternativeCassandraRecord.getAlternativeWords() =
        transcriptionsQueries.getAlternativeWordsQuery.toString()
            .let(findOrStorePreparedStatement).await()
            .bind(audioFileName, hours, minutes, seconds, tenthsOfSecond, alternativePosition)
            .let { wordsStatement -> cassandraClient.executeWithFullFetchAwait(wordsStatement) }
            .map { row -> WordCassandraRecord.fromCassandraRow(row) }

    fun toRecord(audioTranscription: AudioTranscription): AudioTranscriptionCassandraRecord =
        AudioTranscriptionCassandraRecord.fromEntity(audioTranscription)
}

class TranscriptionsQueries {

    companion object {
        private val QUESTION_MARK = QueryBuilder.bindMarker()
    }

    val getMultipleTranscriptionsQuery: Select.Where = QueryBuilder.select()
        .from(TranscriptionCassandraRecord.TABLE_NAME)
        .where(QueryBuilder.eq(TranscriptionCassandraRecord.AUDIO_FILE_NAME_COLUMN, QUESTION_MARK))

    val getTranscriptionAlternativesQuery: Select.Where = QueryBuilder.select()
        .from(AlternativeCassandraRecord.TABLE_NAME)
        .where(QueryBuilder.eq(AlternativeCassandraRecord.AUDIO_FILE_NAME_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(AlternativeCassandraRecord.HOURS_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(AlternativeCassandraRecord.MINUTES_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(AlternativeCassandraRecord.SECONDS_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(AlternativeCassandraRecord.TENTHS_COLUMN, QUESTION_MARK))

    val getAlternativeWordsQuery: Select.Where = QueryBuilder.select()
        .from(WordCassandraRecord.TABLE_NAME)
        .where(QueryBuilder.eq(WordCassandraRecord.AUDIO_FILE_NAME_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(WordCassandraRecord.HOURS_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(WordCassandraRecord.MINUTES_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(WordCassandraRecord.SECONDS_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(WordCassandraRecord.TENTHS_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(WordCassandraRecord.ALTERNATIVE_POSITION_COLUMN, QUESTION_MARK))
}

package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips

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
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip

class AudioClipsCassandraDao(private val cassandraClient: CassandraClient) : CoroutineVerticle() {

    private val audioClipsQueries = AudioClipsQueries()
    private val preparedStatements = mutableMapOf<String, PreparedStatement>()

    private val cassandraMapper = MappingManager.create(cassandraClient)
    private val audioClipRecordTemplate: Mapper<AudioClipCassandraRecord> =
        cassandraMapper.mapper(AudioClipCassandraRecord::class.java)
    private val activeSegmentRecordTemplate: Mapper<ActiveSegmentCassandraRecord> =
        cassandraMapper.mapper(ActiveSegmentCassandraRecord::class.java)

    private val findOrStorePreparedStatement: ((String) -> Deferred<PreparedStatement>) = { statement ->
        async { preparedStatements.getOrPut(statement) { cassandraClient.prepareAwait(statement) } }
    }

    override suspend fun start() {
        audioClipsQueries.getMultipleAudioClipsQuery.toString().let(findOrStorePreparedStatement).await()
        audioClipsQueries.getAudioClipActiveSegmentsQuery.toString().let(findOrStorePreparedStatement).await()
    }

    suspend fun save(audioClipInfoCassandraRecord: AudioClipInfoCassandraRecord) = audioClipInfoCassandraRecord.run {
        audioClipRecordTemplate.saveAwait(audioClipCassandraRecord)
        activeSegmentsCassandraRecords.forEach { activeSegmentRecordTemplate.saveAwait(it) }
    }

    suspend fun findBy(audioFileName: String): List<AudioClipInfoCassandraRecord> =
        audioClipsQueries.getMultipleAudioClipsQuery.toString()
            .let(findOrStorePreparedStatement).await()
            .bind(audioFileName)
            .let { audioClipsStatement ->
                cassandraClient.executeWithFullFetchAwait(audioClipsStatement)
                    .map { row -> AudioClipCassandraRecord.fromCassandraRow(row) }
                    .map { audioClipRecord -> audioClipRecord to audioClipRecord.getAudioClipActiveSegments() }
                    .map { tuple ->
                        AudioClipInfoCassandraRecord(
                            audioClipCassandraRecord = tuple.first,
                            activeSegmentsCassandraRecords = tuple.second
                        )
                    }
            }

    private suspend fun AudioClipCassandraRecord.getAudioClipActiveSegments() =
        audioClipsQueries.getAudioClipActiveSegmentsQuery.toString()
            .let(findOrStorePreparedStatement).await()
            .bind(audioFileName, hours, minutes, seconds, tenthsOfSecond)
            .let { activeSegmentStatement -> cassandraClient.executeWithFullFetchAwait(activeSegmentStatement) }
            .map { row -> ActiveSegmentCassandraRecord.fromCassandraRow(row) }

    fun toRecord(audioClip: AudioClip): AudioClipInfoCassandraRecord =
        AudioClipInfoCassandraRecord.fromEntity(audioClip)
}

class AudioClipsQueries {

    companion object {
        private val QUESTION_MARK = QueryBuilder.bindMarker()
    }

    val getMultipleAudioClipsQuery: Select.Where = QueryBuilder.select()
        .from(AudioClipCassandraRecord.TABLE_NAME)
        .where(QueryBuilder.eq(AudioClipCassandraRecord.AUDIO_FILE_NAME_COLUMN, QUESTION_MARK))

    val getAudioClipActiveSegmentsQuery: Select.Where = QueryBuilder.select()
        .from(ActiveSegmentCassandraRecord.TABLE_NAME)
        .where(QueryBuilder.eq(ActiveSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(ActiveSegmentCassandraRecord.HOURS_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(ActiveSegmentCassandraRecord.MINUTES_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(ActiveSegmentCassandraRecord.SECONDS_COLUMN, QUESTION_MARK))
        .and(QueryBuilder.eq(ActiveSegmentCassandraRecord.TENTHS_COLUMN, QUESTION_MARK))
}

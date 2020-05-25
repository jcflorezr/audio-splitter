package net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo

import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.querybuilder.Select
import io.vertx.cassandra.CassandraClient
import io.vertx.cassandra.Mapper
import io.vertx.cassandra.MappingManager
import io.vertx.kotlin.cassandra.executeAwait
import io.vertx.kotlin.cassandra.oneAwait
import io.vertx.kotlin.cassandra.prepareAwait
import io.vertx.kotlin.cassandra.saveAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.core.exception.PersistenceException

class SourceFileInfoCassandraDao(private val cassandraClient: CassandraClient) : CoroutineVerticle() {

    private val sourceFileInfoQueries = SourceFileInfoQueries()
    private val preparedStatements = mutableMapOf<String, PreparedStatement>()

    private val cassandraMapper = MappingManager.create(cassandraClient)
    private val sourceFileMetadataRecordTemplate: Mapper<SourceFileMetadataCassandraRecord> =
        cassandraMapper.mapper(SourceFileMetadataCassandraRecord::class.java)
    private val sourceFileContentInfoRecordTemplate: Mapper<SourceFileContentInfoCassandraRecord> =
        cassandraMapper.mapper(SourceFileContentInfoCassandraRecord::class.java)

    private val findOrStorePreparedStatement: ((String) -> Deferred<PreparedStatement>) = { statement ->
        async { preparedStatements.getOrPut(statement) { cassandraClient.prepareAwait(statement) } }
    }

    override suspend fun start() {
        sourceFileInfoQueries.getSourceFileMetadataQuery.toString().let(findOrStorePreparedStatement).await()
        sourceFileInfoQueries.getSourceFileContentInfoQuery.toString().let(findOrStorePreparedStatement).await()
    }

    suspend fun save(sourceFileInfoCassandraRecord: SourceFileInfoCassandraRecord) = sourceFileInfoCassandraRecord.run {
        sourceFileMetadataRecordTemplate.saveAwait(sourceFileMetadataCassandraRecord)
        sourceFileContentInfoRecordTemplate.saveAwait(sourceFileContentInfoCassandraRecord)
    }

    suspend fun findBy(audioFileName: String): SourceFileInfoCassandraRecord =
        SourceFileInfoCassandraRecord(
            sourceFileMetadataCassandraRecord = findSourceFileMetadata(audioFileName),
            sourceFileContentInfoCassandraRecord = findSourceFileContentInfo(audioFileName)
        )

    private suspend fun findSourceFileMetadata(audioFileName: String): SourceFileMetadataCassandraRecord =
        sourceFileInfoQueries.getSourceFileMetadataQuery.toString()
            .let(findOrStorePreparedStatement).await()
            .bind(audioFileName)
            .let { audioClipsStatement -> cassandraClient.executeAwait(audioClipsStatement).oneAwait() }
            ?.let { row -> SourceFileMetadataCassandraRecord.fromCassandraRow(row) }
        ?: throw PersistenceException.recordNotFoundInRepository(
            SourceFileMetadataCassandraRecord::class.java.simpleName,
            mapOf(SourceFileMetadataCassandraRecord.AUDIO_FILE_NAME_COLUMN to audioFileName)
        )

    private suspend fun findSourceFileContentInfo(audioFileName: String): SourceFileContentInfoCassandraRecord =
        sourceFileInfoQueries.getSourceFileContentInfoQuery.toString()
            .let(findOrStorePreparedStatement).await()
            .bind(audioFileName)
            .let { audioClipsStatement -> cassandraClient.executeAwait(audioClipsStatement).oneAwait() }
            ?.let { row -> SourceFileContentInfoCassandraRecord.fromCassandraRow(row) }
            ?: throw PersistenceException.recordNotFoundInRepository(
                SourceFileContentInfoCassandraRecord::class.java.simpleName,
                mapOf(SourceFileContentInfoCassandraRecord.AUDIO_FILE_NAME_COLUMN to audioFileName)
            )

    fun toRecord(audioSourceFileInfo: AudioSourceFileInfo) =
        SourceFileInfoCassandraRecord.fromEntity(audioSourceFileInfo)
}

class SourceFileInfoQueries {

    companion object {
        private val QUESTION_MARK = QueryBuilder.bindMarker()
    }

    val getSourceFileMetadataQuery: Select.Where = QueryBuilder.select()
        .from(SourceFileMetadataCassandraRecord.TABLE_NAME)
        .where(QueryBuilder.eq(SourceFileMetadataCassandraRecord.AUDIO_FILE_NAME_COLUMN, QUESTION_MARK))

    val getSourceFileContentInfoQuery: Select.Where = QueryBuilder.select()
        .from(SourceFileContentInfoCassandraRecord.TABLE_NAME)
        .where(QueryBuilder.eq(SourceFileContentInfoCassandraRecord.AUDIO_FILE_NAME_COLUMN, QUESTION_MARK))
}

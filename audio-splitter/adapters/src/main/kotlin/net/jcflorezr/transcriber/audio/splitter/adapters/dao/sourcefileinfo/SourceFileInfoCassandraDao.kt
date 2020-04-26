package net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo

import com.datastax.driver.core.querybuilder.QueryBuilder
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.core.exception.PersistenceException
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.selectOne

class SourceFileInfoCassandraDao(
    private val cassandraTemplate: CassandraOperations
) {

    fun save(sourceFileInfoCassandraRecord: SourceFileInfoCassandraRecord) {
        sourceFileInfoCassandraRecord.run {
            cassandraTemplate.insert(sourceFileMetadataCassandraRecord)
            cassandraTemplate.insert(sourceFileContentInfoCassandraRecord)
        }
    }

    fun findBy(audioFileName: String) =
        SourceFileInfoCassandraRecord(
            sourceFileMetadataCassandraRecord = findSourceFileMetadata(audioFileName),
            sourceFileContentInfoCassandraRecord = findSourceFileContentInfo(audioFileName))

    fun toRecord(audioSourceFileInfo: AudioSourceFileInfo) =
        SourceFileInfoCassandraRecord.fromEntity(audioSourceFileInfo)

    private fun findSourceFileMetadata(audioFileName: String): SourceFileMetadataCassandraRecord =
        QueryBuilder
            .select()
            .from(SourceFileMetadataCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(SourceFileMetadataCassandraRecord.PRIMARY_COLUMN_NAME, audioFileName))
            .let { query -> cassandraTemplate.selectOne<SourceFileMetadataCassandraRecord>(query) }
            ?: throw PersistenceException.recordNotFoundInRepository(
                entityName = SourceFileMetadataCassandraRecord::class.simpleName ?: SourceFileMetadataCassandraRecord.TABLE_NAME,
                keys = mapOf(SourceFileMetadataCassandraRecord.PRIMARY_COLUMN_NAME to audioFileName))

    private fun findSourceFileContentInfo(audioFileName: String): SourceFileContentInfoCassandraRecord =
        QueryBuilder
            .select()
            .from(SourceFileContentInfoCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(SourceFileContentInfoCassandraRecord.PRIMARY_COLUMN_NAME, audioFileName))
            .let { query -> cassandraTemplate.selectOne<SourceFileContentInfoCassandraRecord>(query) }
            ?: throw PersistenceException.recordNotFoundInRepository(
                entityName = SourceFileMetadataCassandraRecord::class.simpleName ?: SourceFileMetadataCassandraRecord.TABLE_NAME,
                keys = mapOf(SourceFileMetadataCassandraRecord.PRIMARY_COLUMN_NAME to audioFileName))
}

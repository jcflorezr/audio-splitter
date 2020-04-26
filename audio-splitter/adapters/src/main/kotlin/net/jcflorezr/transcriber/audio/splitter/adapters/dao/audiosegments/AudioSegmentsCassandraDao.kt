package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import com.datastax.driver.core.querybuilder.QueryBuilder
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.core.exception.PersistenceException
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.select
import org.springframework.data.cassandra.core.selectOne

class AudioSegmentsCassandraDao(
    private val cassandraTemplate: CassandraOperations
) {

    fun save(audioSegmentCassandraRecord: AudioSegmentCassandraRecord) {
        cassandraTemplate.insert(audioSegmentCassandraRecord)
    }

    fun findBy(audioFileName: String, segmentStartInSeconds: Float): AudioSegmentCassandraRecord =
        QueryBuilder
            .select()
            .from(AudioSegmentCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(AudioSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN, audioFileName))
            .and(QueryBuilder.eq(AudioSegmentCassandraRecord.SEGMENT_START_IN_SECONDS_COLUMN, segmentStartInSeconds))
            .let { query -> cassandraTemplate.selectOne<AudioSegmentCassandraRecord>(query) }
            ?: throw PersistenceException.recordNotFoundInRepository(
                entityName = AudioSegmentCassandraRecord::class.simpleName ?: AudioSegmentCassandraRecord.TABLE_NAME,
                keys = mapOf(
                    AudioSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN to audioFileName,
                    AudioSegmentCassandraRecord.SEGMENT_START_IN_SECONDS_COLUMN to segmentStartInSeconds))

    fun findRange(
        audioFileName: String,
        segmentStartInSeconds: Float,
        segmentEndInSeconds: Float
    ): Sequence<AudioSegmentCassandraRecord> =
        QueryBuilder
            .select()
            .from(AudioSegmentCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(AudioSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN, audioFileName))
            .and(QueryBuilder.gte(AudioSegmentCassandraRecord.SEGMENT_START_IN_SECONDS_COLUMN, segmentStartInSeconds))
            .and(QueryBuilder.lte(AudioSegmentCassandraRecord.SEGMENT_START_IN_SECONDS_COLUMN, segmentEndInSeconds))
            .let { query -> cassandraTemplate.select<AudioSegmentCassandraRecord>(query).asSequence() }

    fun toRecord(audioSegment: AudioSegment) = AudioSegmentCassandraRecord.fromEntity(audioSegment)
}

package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips

import com.datastax.driver.core.querybuilder.QueryBuilder
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.core.exception.PersistenceException
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.select
import org.springframework.data.cassandra.core.selectOne

class AudioClipsCassandraDao(
    private val cassandraTemplate: CassandraOperations
) {

    fun save(audioClipInfoCassandraRecord: AudioClipInfoCassandraRecord) {
        cassandraTemplate.insert(audioClipInfoCassandraRecord.audioClipCassandraRecord)
        audioClipInfoCassandraRecord.activeSegmentsCassandraRecords.forEach { activeSegmentRecord ->
            cassandraTemplate.insert(activeSegmentRecord)
        }
    }

    fun findBy(audioFileName: String, hours: Int, minutes: Int, seconds: Int, tenthsOfSecond: Int): AudioClipInfoCassandraRecord =
        findAudioClipInfoBy(audioFileName, hours, minutes, seconds, tenthsOfSecond).let { audioClipRecord ->
            AudioClipInfoCassandraRecord(
                audioClipCassandraRecord = audioClipRecord,
                activeSegmentsCassandraRecords = findAudioClipActiveSegmentsBy(audioClipRecord))
        }

    private fun findAudioClipInfoBy(audioFileName: String, hours: Int, minutes: Int, seconds: Int, tenthsOfSecond: Int): AudioClipCassandraRecord =
        QueryBuilder
            .select()
            .from(AudioClipCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(AudioClipCassandraRecord.AUDIO_FILE_NAME_COLUMN, audioFileName))
            .and(QueryBuilder.eq(AudioClipCassandraRecord.HOURS_COLUMN, hours))
            .and(QueryBuilder.eq(AudioClipCassandraRecord.MINUTES_COLUMN, minutes))
            .and(QueryBuilder.eq(AudioClipCassandraRecord.SECONDS_COLUMN, seconds))
            .and(QueryBuilder.eq(AudioClipCassandraRecord.TENTHS_COLUMN, tenthsOfSecond))
            .let { query -> cassandraTemplate.selectOne<AudioClipCassandraRecord>(query) }
            ?: throw PersistenceException.recordNotFoundInRepository(
                entityName = AudioClipInfoCassandraRecord::class.simpleName ?: AudioClipCassandraRecord.TABLE_NAME,
                keys = mapOf(
                    AudioClipCassandraRecord.AUDIO_FILE_NAME_COLUMN to audioFileName,
                    AudioClipCassandraRecord.HOURS_COLUMN to hours,
                    AudioClipCassandraRecord.MINUTES_COLUMN to minutes,
                    AudioClipCassandraRecord.SECONDS_COLUMN to seconds,
                    AudioClipCassandraRecord.TENTHS_COLUMN to tenthsOfSecond))

    fun findBy(audioFileName: String): Sequence<AudioClipInfoCassandraRecord> =
        findAudioClipInfoBy(audioFileName).map { audioClipRecord ->
            AudioClipInfoCassandraRecord(
                audioClipCassandraRecord = audioClipRecord,
                activeSegmentsCassandraRecords = findAudioClipActiveSegmentsBy(audioClipRecord))
        }

    private fun findAudioClipInfoBy(audioFileName: String): Sequence<AudioClipCassandraRecord> =
        QueryBuilder
            .select()
            .from(AudioClipCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(AudioClipCassandraRecord.AUDIO_FILE_NAME_COLUMN, audioFileName))
            .let { query -> cassandraTemplate.select<AudioClipCassandraRecord>(query).asSequence() }

    private fun findAudioClipActiveSegmentsBy(
        audioClipCassandraRecord: AudioClipCassandraRecord
    ): Sequence<ActiveSegmentCassandraRecord> = audioClipCassandraRecord.primaryKey.run {
        QueryBuilder
            .select()
            .from(ActiveSegmentCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(ActiveSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN, audioFileName))
            .and(QueryBuilder.eq(ActiveSegmentCassandraRecord.HOURS_COLUMN, hours))
            .and(QueryBuilder.eq(ActiveSegmentCassandraRecord.MINUTES_COLUMN, minutes))
            .and(QueryBuilder.eq(ActiveSegmentCassandraRecord.SECONDS_COLUMN, seconds))
            .and(QueryBuilder.eq(ActiveSegmentCassandraRecord.TENTHS_COLUMN, tenthsOfSecond))
            .let { query -> cassandraTemplate.select<ActiveSegmentCassandraRecord>(query).asSequence() }
    }

    fun toRecord(audioClip: AudioClip) = AudioClipInfoCassandraRecord.fromEntity(audioClip)
}

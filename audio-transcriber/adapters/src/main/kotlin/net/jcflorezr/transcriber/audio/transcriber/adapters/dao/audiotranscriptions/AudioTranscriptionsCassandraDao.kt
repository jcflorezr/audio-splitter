package net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions

import com.datastax.driver.core.querybuilder.QueryBuilder
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.core.exception.PersistenceException
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.select
import org.springframework.data.cassandra.core.selectOne

class AudioTranscriptionsCassandraDao(
    private val cassandraTemplate: CassandraOperations
) {

    fun save(audioTranscriptionCassandraRecord: AudioTranscriptionCassandraRecord) {
        cassandraTemplate.insert(audioTranscriptionCassandraRecord.transcriptionCassandraRecord)
        audioTranscriptionCassandraRecord.alternativesCassandraRecordWrapper.forEach { alternativeRecordWrapper ->
            cassandraTemplate.insert(alternativeRecordWrapper.alternativeCassandraRecord)
            alternativeRecordWrapper.wordsCassandraRecord.forEach { wordRecord ->
                cassandraTemplate.insert(wordRecord)
            }
        }
    }

    fun findBy(audioFileName: String, hours: Int, minutes: Int, seconds: Int, tenthsOfSecond: Int): AudioTranscriptionCassandraRecord =
        findAudioTranscriptionBy(audioFileName, hours, minutes, seconds, tenthsOfSecond)
            .let { transcriptionRecord ->
                findTranscriptionAlternativesBy(transcriptionRecord).asSequence()
                    .map { alternativeRecord ->
                        alternativeRecord to findTranscriptionAlternativesWordsBy(alternativeRecord) }
                    .map { (alternativeRecord, alternativeWordsRecord) ->
                        AlternativeCassandraRecordWrapper(alternativeRecord, alternativeWordsRecord) }
                    .let { alternatives -> AudioTranscriptionCassandraRecord(transcriptionRecord, alternatives) }
            }

    private fun findAudioTranscriptionBy(audioFileName: String, hours: Int, minutes: Int, seconds: Int, tenthsOfSecond: Int): TranscriptionCassandraRecord =
        QueryBuilder
            .select()
            .from(TranscriptionCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(TranscriptionCassandraRecord.AUDIO_FILE_NAME_COLUMN, audioFileName))
            .and(QueryBuilder.eq(TranscriptionCassandraRecord.HOURS_COLUMN, hours))
            .and(QueryBuilder.eq(TranscriptionCassandraRecord.MINUTES_COLUMN, minutes))
            .and(QueryBuilder.eq(TranscriptionCassandraRecord.SECONDS_COLUMN, seconds))
            .and(QueryBuilder.eq(TranscriptionCassandraRecord.TENTHS_COLUMN, tenthsOfSecond))
            .let { query -> cassandraTemplate.selectOne<TranscriptionCassandraRecord>(query) }
            ?: throw PersistenceException.recordNotFoundInRepository(
                entityName = AudioTranscriptionCassandraRecord::class.simpleName ?: TranscriptionCassandraRecord.TABLE_NAME,
                keys = mapOf(
                    TranscriptionCassandraRecord.AUDIO_FILE_NAME_COLUMN to audioFileName,
                    TranscriptionCassandraRecord.HOURS_COLUMN to hours,
                    TranscriptionCassandraRecord.MINUTES_COLUMN to minutes,
                    TranscriptionCassandraRecord.SECONDS_COLUMN to seconds,
                    TranscriptionCassandraRecord.TENTHS_COLUMN to tenthsOfSecond))

    fun findBy(audioFileName: String): Sequence<AudioTranscriptionCassandraRecord> =
        findAudioTranscriptionBy(audioFileName)
            .map { transcriptionRecord ->
                findTranscriptionAlternativesBy(transcriptionRecord).asSequence()
                    .map { alternativeRecord ->
                        alternativeRecord to findTranscriptionAlternativesWordsBy(alternativeRecord) }
                    .map { (alternativeRecord, alternativeWordsRecord) ->
                        AlternativeCassandraRecordWrapper(alternativeRecord, alternativeWordsRecord) }
                    .let { alternatives -> AudioTranscriptionCassandraRecord(transcriptionRecord, alternatives) }
            }

    private fun findAudioTranscriptionBy(audioFileName: String): Sequence<TranscriptionCassandraRecord> =
        QueryBuilder
            .select()
            .from(TranscriptionCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(TranscriptionCassandraRecord.AUDIO_FILE_NAME_COLUMN, audioFileName))
            .let { query -> cassandraTemplate.select<TranscriptionCassandraRecord>(query).asSequence() }

    private fun findTranscriptionAlternativesBy(
        transcriptionCassandraRecord: TranscriptionCassandraRecord
    ): Sequence<AlternativeCassandraRecord> = transcriptionCassandraRecord.primaryKey.run {
        QueryBuilder
            .select()
            .from(AlternativeCassandraRecord.TABLE_NAME)
            .where(QueryBuilder.eq(AlternativeCassandraRecord.AUDIO_FILE_NAME_COLUMN, audioFileName))
            .and(QueryBuilder.eq(AlternativeCassandraRecord.HOURS_COLUMN, hours))
            .and(QueryBuilder.eq(AlternativeCassandraRecord.MINUTES_COLUMN, minutes))
            .and(QueryBuilder.eq(AlternativeCassandraRecord.SECONDS_COLUMN, seconds))
            .and(QueryBuilder.eq(AlternativeCassandraRecord.TENTHS_COLUMN, tenthsOfSecond))
            .let { query -> cassandraTemplate.select<AlternativeCassandraRecord>(query).asSequence() }
    }

    private fun findTranscriptionAlternativesWordsBy(
        alternativeCassandraRecord: AlternativeCassandraRecord
    ): Sequence<WordCassandraRecord> =
        alternativeCassandraRecord.primaryKey.run {
            QueryBuilder
                .select()
                .from(WordCassandraRecord.TABLE_NAME)
                .where(QueryBuilder.eq(WordCassandraRecord.AUDIO_FILE_NAME_COLUMN, audioFileName))
                .and(QueryBuilder.eq(WordCassandraRecord.HOURS_COLUMN, hours))
                .and(QueryBuilder.eq(WordCassandraRecord.MINUTES_COLUMN, minutes))
                .and(QueryBuilder.eq(WordCassandraRecord.SECONDS_COLUMN, seconds))
                .and(QueryBuilder.eq(WordCassandraRecord.TENTHS_COLUMN, tenthsOfSecond))
                .let { query -> cassandraTemplate.select<WordCassandraRecord>(query).asSequence() }
        }

    fun toRecord(audioTranscription: AudioTranscription) = AudioTranscriptionCassandraRecord.fromEntity(audioTranscription)
}
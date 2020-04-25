package net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions

import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.Alternative
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.WordInfo
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table

data class AudioTranscriptionCassandraRecord(
    val transcriptionCassandraRecord: TranscriptionCassandraRecord,
    val alternativesCassandraRecordWrapper: Sequence<AlternativeCassandraRecordWrapper>
) {
    companion object {

        fun fromEntity(audioTranscription: AudioTranscription): AudioTranscriptionCassandraRecord {
            val transcriptionRecord = TranscriptionCassandraRecord.fromEntity(audioTranscription)
            val alternativesRecords = audioTranscription.alternatives.asSequence()
                .map { alternative ->
                    AlternativeCassandraRecordWrapper.fromEntity(transcriptionRecord, alternative)
                }
            return AudioTranscriptionCassandraRecord(transcriptionRecord, alternativesRecords)
        }
    }

    fun translate() = transcriptionCassandraRecord.translate(alternativesCassandraRecordWrapper)
}

data class AlternativeCassandraRecordWrapper(
    val alternativeCassandraRecord: AlternativeCassandraRecord,
    val wordsCassandraRecord: Sequence<WordCassandraRecord>
) {
    companion object {

        fun fromEntity(
            transcriptionCassandraRecord: TranscriptionCassandraRecord,
            alternative: Alternative
        ): AlternativeCassandraRecordWrapper {
            val alternativeRecord = AlternativeCassandraRecord.fromEntity(
                AlternativePrimaryKey.createNew(transcriptionCassandraRecord.primaryKey, alternative.position), alternative)
            val wordRecord =
                alternative.words?.asSequence()
                    ?.map { word -> WordCassandraRecord.fromEntity(
                        WordPrimaryKey.createNew(alternativeRecord.primaryKey, word.position), word) }
                    ?: emptySequence()
            return AlternativeCassandraRecordWrapper(alternativeRecord, wordRecord)
        }
    }
}

@Table(value = "audio_transcription")
data class TranscriptionCassandraRecord(
    @PrimaryKey val primaryKey: TranscriptionPrimaryKey
) {
    companion object {
        const val TABLE_NAME = "audio_transcription"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val HOURS_COLUMN = "hours"
        const val MINUTES_COLUMN = "minutes"
        const val SECONDS_COLUMN = "seconds"
        const val TENTHS_COLUMN = "tenths_of_second"

        fun fromEntity(audioTranscription: AudioTranscription) = audioTranscription.run {
            TranscriptionCassandraRecord(
                TranscriptionPrimaryKey(sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond))
        }
    }

    fun translate(alternativesRecords: Sequence<AlternativeCassandraRecordWrapper>) = primaryKey.run {
        val alternatives = alternativesRecords
            .map { record -> record.alternativeCassandraRecord.translate(record.wordsCassandraRecord) }
            .toList()
        AudioTranscription(audioFileName, hours, minutes, seconds, tenthsOfSecond, alternatives)
    }
}

@Table(value = "audio_transcription_alternative")
data class AlternativeCassandraRecord(
    @PrimaryKey val primaryKey: AlternativePrimaryKey,
    @Column("transcription") val transcription: String,
    @Column("confidence") val confidence: Float?
) {
    companion object {
        const val TABLE_NAME = "audio_transcription_alternative"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val HOURS_COLUMN = "hours"
        const val MINUTES_COLUMN = "minutes"
        const val SECONDS_COLUMN = "seconds"
        const val TENTHS_COLUMN = "tenths_of_second"
        const val ALTERNATIVE_POSITION_COLUMN = "alternative_position"

        fun fromEntity(alternativePrimaryKey: AlternativePrimaryKey, alternative: Alternative) = alternative.run {
            AlternativeCassandraRecord(alternativePrimaryKey, transcription, confidence)
        }
    }

    fun translate(
        wordsRecords: Sequence<WordCassandraRecord>
    ) = Alternative.Builder(primaryKey.alternativePosition, transcription)
            .confidence(confidence)
            .words(wordsRecords.map { it.translate() }.toList())
            .build()
}

@Table(value = "audio_transcription_alternative_word")
data class WordCassandraRecord(
    @PrimaryKey val primaryKey: WordPrimaryKey,
    @Column("word") val word: String,
    @Column("from_time") val from: Float,
    @Column("to_time") val to: Float
) {
    companion object {
        const val TABLE_NAME = "audio_transcription_alternative_word"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val HOURS_COLUMN = "hours"
        const val MINUTES_COLUMN = "minutes"
        const val SECONDS_COLUMN = "seconds"
        const val TENTHS_COLUMN = "tenths_of_second"
        const val ALTERNATIVE_POSITION_COLUMN = "alternative_position"
        const val ALTERNATIVE_WORD_POSITION_COLUMN = "alternative_word_position"

        fun fromEntity(primaryKey: WordPrimaryKey, wordInfo: WordInfo) = wordInfo.run {
            WordCassandraRecord(primaryKey, word, from, to)
        }
    }

    fun translate() = WordInfo(primaryKey.wordPosition, word, from, to)
}

@PrimaryKeyClass
data class TranscriptionPrimaryKey(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @PrimaryKeyColumn(name = "hours", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val hours: Int,
    @PrimaryKeyColumn(name = "minutes", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    val minutes: Int,
    @PrimaryKeyColumn(name = "seconds", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    val seconds: Int,
    @PrimaryKeyColumn(name = "tenths_of_second", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    val tenthsOfSecond: Int
)

@PrimaryKeyClass
data class AlternativePrimaryKey(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @PrimaryKeyColumn(name = "hours", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val hours: Int,
    @PrimaryKeyColumn(name = "minutes", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    val minutes: Int,
    @PrimaryKeyColumn(name = "seconds", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    val seconds: Int,
    @PrimaryKeyColumn(name = "tenths_of_second", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    val tenthsOfSecond: Int,
    @PrimaryKeyColumn(name = "alternative_position", ordinal = 5, type = PrimaryKeyType.CLUSTERED)
    val alternativePosition: Int
) {
    companion object {
        fun createNew(transcriptionPrimaryKey: TranscriptionPrimaryKey, alternativePosition: Int) = transcriptionPrimaryKey.run {
            AlternativePrimaryKey(audioFileName, hours, minutes, seconds, tenthsOfSecond, alternativePosition)
        }
    }
}

@PrimaryKeyClass
data class WordPrimaryKey(
    @PrimaryKeyColumn(name = "audio_file_name", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val audioFileName: String,
    @PrimaryKeyColumn(name = "hours", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    val hours: Int,
    @PrimaryKeyColumn(name = "minutes", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    val minutes: Int,
    @PrimaryKeyColumn(name = "seconds", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
    val seconds: Int,
    @PrimaryKeyColumn(name = "tenths_of_second", ordinal = 4, type = PrimaryKeyType.CLUSTERED)
    val tenthsOfSecond: Int,
    @PrimaryKeyColumn(name = "alternative_position", ordinal = 5, type = PrimaryKeyType.CLUSTERED)
    val alternativePosition: Int,
    @PrimaryKeyColumn(name = "alternative_word_position", ordinal = 6, type = PrimaryKeyType.CLUSTERED)
    val wordPosition: Int
) {
    companion object {
        fun createNew(alternativePrimaryKey: AlternativePrimaryKey, wordPosition: Int) = alternativePrimaryKey.run {
            WordPrimaryKey(audioFileName, hours, minutes, seconds, tenthsOfSecond, alternativePosition, wordPosition)
        }
    }
}

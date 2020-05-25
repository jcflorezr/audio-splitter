package net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions

import com.datastax.driver.core.Row
import com.datastax.driver.mapping.annotations.ClusteringColumn
import com.datastax.driver.mapping.annotations.Column
import com.datastax.driver.mapping.annotations.PartitionKey
import com.datastax.driver.mapping.annotations.Table
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.Alternative
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.WordInfo

data class AudioTranscriptionCassandraRecord(
    val transcriptionCassandraRecord: TranscriptionCassandraRecord,
    val alternativesCassandraRecord: List<AlternativeCassandraRecord>,
    val wordsCassandraRecord: List<WordCassandraRecord>
) {
    companion object {

        fun fromEntity(audioTranscription: AudioTranscription): AudioTranscriptionCassandraRecord {
            val transcriptionRecord = TranscriptionCassandraRecord.fromEntity(audioTranscription)
            val (alternativesRecords, wordsRecords) = audioTranscription.alternatives
                .map { alternative ->
                    val currentAlternativeRecord = AlternativeCassandraRecord.fromEntity(transcriptionRecord, alternative)
                    val currentAlternativeWordsRecords = alternative.words
                        ?.map { w -> WordCassandraRecord.fromEntity(currentAlternativeRecord, w) }
                        ?: emptyList()
                    currentAlternativeRecord to currentAlternativeWordsRecords
                }.let { alternativesRecords ->
                    alternativesRecords.map { it.first } to alternativesRecords.flatMap { it.second }
                }
            return AudioTranscriptionCassandraRecord(transcriptionRecord, alternativesRecords, wordsRecords)
        }
    }

    fun translate(): AudioTranscription {
        val words = wordsCassandraRecord.map { it.translate() }
        val alternatives = alternativesCassandraRecord.map { it.translate(words) }
        return transcriptionCassandraRecord.translate(alternatives)
    }
}

@Table(name = "audio_transcription")
data class TranscriptionCassandraRecord(
    @PartitionKey(0) @Column(name = "audio_file_name") val audioFileName: String,
    @ClusteringColumn(0) @Column(name = "hours") val hours: Int,
    @ClusteringColumn(1) @Column(name = "minutes") val minutes: Int,
    @ClusteringColumn(2) @Column(name = "seconds") val seconds: Int,
    @ClusteringColumn(3) @Column(name = "tenths_of_second") val tenthsOfSecond: Int
) {
    companion object {
        const val TABLE_NAME = "audio_transcription"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val HOURS_COLUMN = "hours"
        const val MINUTES_COLUMN = "minutes"
        const val SECONDS_COLUMN = "seconds"
        const val TENTHS_COLUMN = "tenths_of_second"

        fun fromEntity(audioTranscription: AudioTranscription) = audioTranscription.run {
            TranscriptionCassandraRecord(sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond)
        }

        fun fromCassandraRow(row: Row) =
            TranscriptionCassandraRecord(
                audioFileName = row.getString(AUDIO_FILE_NAME_COLUMN),
                hours = row.getInt(HOURS_COLUMN),
                minutes = row.getInt(MINUTES_COLUMN),
                seconds = row.getInt(SECONDS_COLUMN),
                tenthsOfSecond = row.getInt(TENTHS_COLUMN)
            )
    }

    fun translate(alternativesRecords: List<Alternative>) =
        AudioTranscription(audioFileName, hours, minutes, seconds, tenthsOfSecond, alternativesRecords)
}

@Table(name = "audio_transcription_alternative")
data class AlternativeCassandraRecord(
    @PartitionKey(0) @Column(name = "audio_file_name") val audioFileName: String,
    @ClusteringColumn(0) @Column(name = "hours") val hours: Int,
    @ClusteringColumn(1) @Column(name = "minutes") val minutes: Int,
    @ClusteringColumn(2) @Column(name = "seconds") val seconds: Int,
    @ClusteringColumn(3) @Column(name = "tenths_of_second") val tenthsOfSecond: Int,
    @ClusteringColumn(4) @Column(name = "alternative_position") val alternativePosition: Int,
    @Column(name = "transcription") val transcription: String,
    @Column(name = "confidence") val confidence: Float?
) {
    companion object {
        const val TABLE_NAME = "audio_transcription_alternative"
        const val AUDIO_FILE_NAME_COLUMN = "audio_file_name"
        const val HOURS_COLUMN = "hours"
        const val MINUTES_COLUMN = "minutes"
        const val SECONDS_COLUMN = "seconds"
        const val TENTHS_COLUMN = "tenths_of_second"
        const val ALTERNATIVE_POSITION_COLUMN = "alternative_position"
        const val TRANSCRIPTION_COLUMN = "transcription"
        const val CONFIDENCE_COLUMN = "confidence"

        fun fromEntity(transcriptionCassandraRecord: TranscriptionCassandraRecord, alternative: Alternative) =
            AlternativeCassandraRecord(
                transcriptionCassandraRecord.audioFileName,
                transcriptionCassandraRecord.hours,
                transcriptionCassandraRecord.minutes,
                transcriptionCassandraRecord.seconds,
                transcriptionCassandraRecord.tenthsOfSecond,
                alternative.position,
                alternative.transcription,
                alternative.confidence
            )

        fun fromCassandraRow(row: Row) =
            AlternativeCassandraRecord(
                audioFileName = row.getString(AUDIO_FILE_NAME_COLUMN),
                hours = row.getInt(HOURS_COLUMN),
                minutes = row.getInt(MINUTES_COLUMN),
                seconds = row.getInt(SECONDS_COLUMN),
                tenthsOfSecond = row.getInt(TENTHS_COLUMN),
                alternativePosition = row.getInt(ALTERNATIVE_POSITION_COLUMN),
                transcription = row.getString(TRANSCRIPTION_COLUMN),
                confidence = row.getFloat(CONFIDENCE_COLUMN)
            )
    }

    fun translate(wordsRecords: List<WordInfo>) =
        Alternative.Builder(alternativePosition, transcription)
            .confidence(confidence)
            .words(wordsRecords)
            .build()
}

@Table(name = "audio_transcription_alternative_word")
data class WordCassandraRecord(
    @PartitionKey(0) @Column(name = "audio_file_name") val audioFileName: String,
    @ClusteringColumn(0) @Column(name = "hours") val hours: Int,
    @ClusteringColumn(1) @Column(name = "minutes") val minutes: Int,
    @ClusteringColumn(2) @Column(name = "seconds") val seconds: Int,
    @ClusteringColumn(3) @Column(name = "tenths_of_second") val tenthsOfSecond: Int,
    @ClusteringColumn(4) @Column(name = "alternative_position") val alternativePosition: Int,
    @ClusteringColumn(5) @Column(name = "alternative_word_position") val wordPosition: Int,
    @Column(name = "word") val word: String,
    @Column(name = "from_time") val from: Float,
    @Column(name = "to_time") val to: Float
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
        const val WORD_COLUMN = "word"
        const val FROM_TIME_COLUMN = "from_time"
        const val TO_TIME_COLUMN = "to_time"

        fun fromEntity(alternativeCassandraRecord: AlternativeCassandraRecord, wordInfo: WordInfo) =
            WordCassandraRecord(
                alternativeCassandraRecord.audioFileName,
                alternativeCassandraRecord.hours,
                alternativeCassandraRecord.minutes,
                alternativeCassandraRecord.seconds,
                alternativeCassandraRecord.tenthsOfSecond,
                alternativeCassandraRecord.alternativePosition,
                wordInfo.position,
                wordInfo.word,
                wordInfo.from,
                wordInfo.to
            )

        fun fromCassandraRow(row: Row) =
            WordCassandraRecord(
                audioFileName = row.getString(AUDIO_FILE_NAME_COLUMN),
                hours = row.getInt(HOURS_COLUMN),
                minutes = row.getInt(MINUTES_COLUMN),
                seconds = row.getInt(SECONDS_COLUMN),
                tenthsOfSecond = row.getInt(TENTHS_COLUMN),
                alternativePosition = row.getInt(ALTERNATIVE_POSITION_COLUMN),
                wordPosition = row.getInt(ALTERNATIVE_WORD_POSITION_COLUMN),
                word = row.getString(WORD_COLUMN),
                from = row.getFloat(FROM_TIME_COLUMN),
                to = row.getFloat(TO_TIME_COLUMN)
            )
    }

    fun translate() = WordInfo(wordPosition, word, from, to)
}

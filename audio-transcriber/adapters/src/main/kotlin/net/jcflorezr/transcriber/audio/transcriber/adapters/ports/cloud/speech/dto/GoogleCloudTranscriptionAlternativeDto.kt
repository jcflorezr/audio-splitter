package net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.dto

import com.google.cloud.speech.v1.SpeechRecognitionAlternative
import com.google.protobuf.Duration
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.Alternative
import net.jcflorezr.transcriber.core.util.FloatingPointUtils.tenthsSecondsFormat

data class GoogleCloudTranscriptionAlternativeDto private constructor(
    val position: Int,
    val transcription: String,
    val confidence: Float?,
    val words: List<WordInfo>?
) {

    data class Builder(
        private var position: Int,
        private var transcription: String,
        private var confidence: Float? = null,
        private var words: List<WordInfo>? = null
    ) {
        fun position(position: Int) = apply { this.position = position }
        fun transcription(transcription: String) = apply { this.transcription = transcription }
        fun confidence(confidence: Float) = apply { this.confidence = confidence }
        fun words(words: List<WordInfo>) = apply { this.words = words }

        fun build() = GoogleCloudTranscriptionAlternativeDto(position, transcription, confidence, words)
    }

    companion object {

        fun from(index: Int, alternative: SpeechRecognitionAlternative) =
            Builder(position = index + 1, transcription = alternative.transcript)
                .confidence(alternative.confidence)
                .words(alternative.wordsList
                    .mapIndexed { wordIndex, currentWord ->
                        WordInfo(
                            position = wordIndex + 1,
                            word = currentWord.word,
                            startTime = currentWord.takeIf { it.hasStartTime() }?.run { startTime.getTime() },
                            endTime = currentWord.takeIf { it.hasEndTime() }?.run { endTime.getTime() })
                    })
                .build()

        private fun Duration.getTime() = Time(
            seconds = seconds,
            tenths = nanos / 100000000,
            millis = nanos / 1000000,
            nanos = nanos)
    }

    fun toEntity() =
        Alternative.Builder(position, transcription)
            .confidence(confidence)
            .words(words =
                words?.map {
                    val wordStartTimeSeconds = getWordTimeSeconds(it.startTime?.seconds)
                    val wordStartTimeTenths = wordTimeTenths(it.startTime?.tenths)
                    val wordEndTimeSeconds = getWordTimeSeconds(it.endTime?.seconds)
                    val wordEndTimeTenths = wordTimeTenths(it.endTime?.tenths)
                    net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.WordInfo(
                        position = it.position,
                        word = it.word,
                        from = tenthsSecondsFormat(wordStartTimeSeconds + wordStartTimeTenths).toFloat(),
                        to = tenthsSecondsFormat(wordEndTimeSeconds + wordEndTimeTenths).toFloat())
                })
            .build()

    private fun getWordTimeSeconds(timeSeconds: Long?) = timeSeconds?.toFloat() ?: 0.0f

    private fun wordTimeTenths(timeTenths: Int?): Float {
        return tenthsSecondsFormat((timeTenths?.toFloat() ?: 0.0f) / 10.0f).toFloat()
    }
}

data class WordInfo(
    val position: Int,
    val word: String,
    val startTime: Time?,
    val endTime: Time?
)

data class Time(
    val seconds: Long,
    val tenths: Int,
    val millis: Int,
    val nanos: Int
)
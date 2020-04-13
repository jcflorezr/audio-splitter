package net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions

import net.jcflorezr.transcriber.core.domain.AggregateRoot

data class AudioTranscription(
    val sourceAudioFileName: String,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int,
    val alternatives: List<Alternative>
) : AggregateRoot {

    companion object {

        fun createNew(
            generatedAudioClip: GeneratedAudioClip,
            alternatives: List<Alternative>
        ) = generatedAudioClip
            .run { AudioTranscription(sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond, alternatives) }
    }
}

data class Alternative(
    val transcription: String,
    val confidence: Float?,
    val words: List<WordInfo>?
) {

    data class Builder(
        private var transcription: String,
        private var confidence: Float? = null,
        private var words: List<WordInfo>? = null
    ) {
        fun transcription(transcription: String) = apply { this.transcription = transcription }
        fun confidence(confidence: Float?) = apply { this.confidence = confidence }
        fun words(words: List<WordInfo>?) = apply { this.words = words }

        fun build() = Alternative(transcription, confidence, words)
    }
}

data class WordInfo(val word: String, val from: Float, val to: Float)
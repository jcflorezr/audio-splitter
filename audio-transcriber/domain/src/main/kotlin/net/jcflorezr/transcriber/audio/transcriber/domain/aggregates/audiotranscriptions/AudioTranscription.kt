package net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions

import net.jcflorezr.transcriber.audio.transcriber.domain.exception.AudioTranscriptionException
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo
import net.jcflorezr.transcriber.core.util.CollectionUtils

/*
    Entity (Aggregate Root)
 */
data class AudioTranscription(
    val sourceAudioFileName: String,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int,
    val alternatives: List<Alternative>
) : AggregateRoot {

    companion object {

        fun createNew(audioClipFileInfo: AudioClipFileInfo, alternatives: List<Alternative>) = audioClipFileInfo.run {
            alternatives.checkDuplicatePositions(audioClipFileInfo)
            AudioTranscription(sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond, alternatives)
        }

        private fun List<Alternative>.checkDuplicatePositions(audioClipFileInfo: AudioClipFileInfo) {
            onEach { alternative -> alternative.checkDuplicateWordsPositions(audioClipFileInfo) }
            .map { alternative -> alternative.position }
            .let { alternativesPositions -> CollectionUtils.findDuplicates(alternativesPositions) }
            .takeIf { duplicates -> duplicates.keys.isNotEmpty() }
            ?.also { duplicates ->
                throw AudioTranscriptionException.duplicateAlternativesPositions(audioClipFileInfo, duplicates.keys) }
        }
    }
}

/*
    Entity
 */
data class Alternative private constructor(
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
        fun confidence(confidence: Float?) = apply { this.confidence = confidence }
        fun words(words: List<WordInfo>?) = apply { this.words = words }

        fun build() = Alternative(position, transcription, confidence, words)
    }

    fun checkDuplicateWordsPositions(audioClipFileInfo: AudioClipFileInfo) {
        words
            ?.map { wordInfo -> wordInfo.position }
            ?.let { wordsPositions -> CollectionUtils.findDuplicates(wordsPositions) }
            ?.takeIf { duplicates -> duplicates.keys.isNotEmpty() }
            ?.also { duplicates -> throw AudioTranscriptionException.duplicateAlternativeWordsPositions(
                audioClipFileInfo = audioClipFileInfo,
                alternativePosition =  this.position,
                duplicatePositions = duplicates.keys) }
    }
}

/*
    Entity
 */
data class WordInfo(val position: Int, val word: String, val from: Float, val to: Float)
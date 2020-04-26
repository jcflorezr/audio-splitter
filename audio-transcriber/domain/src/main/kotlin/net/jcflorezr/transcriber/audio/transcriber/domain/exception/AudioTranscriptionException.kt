package net.jcflorezr.transcriber.audio.transcriber.domain.exception

import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo
import net.jcflorezr.transcriber.core.exception.InternalServerErrorException

class AudioTranscriptionException(message: String) : RuntimeException(message) {

    companion object {

        fun duplicateAlternativesPositions(audioClipFileInfo: AudioClipFileInfo, duplicatePositions: Set<Int>) = audioClipFileInfo.run {
            InternalServerErrorException(
                errorCode = "duplicate_alternatives_positions_in_transcription",
                exception = AudioTranscriptionException(
                    "Transcription for $sourceAudioFileName located in (hours=$hours minutes=$minutes seconds=$seconds tenths=$tenthsOfSecond) " +
                    "has the following duplicate positions numbers in the list of alternatives: $duplicatePositions"))
        }

        fun duplicateAlternativeWordsPositions(
            audioClipFileInfo: AudioClipFileInfo,
            alternativePosition: Int,
            duplicatePositions: Set<Int>
        ) = audioClipFileInfo.run {
            InternalServerErrorException(
                errorCode = "duplicate_alternative_words_positions_in_transcription",
                exception = AudioTranscriptionException(
                    "Transcription for $sourceAudioFileName located in (hours=$hours minutes=$minutes seconds=$seconds tenths=$tenthsOfSecond) " +
                    "has duplicate words positions in the alternative with position=$alternativePosition." +
                    "Duplicate words positions numbers: $duplicatePositions"))
        }
    }
}
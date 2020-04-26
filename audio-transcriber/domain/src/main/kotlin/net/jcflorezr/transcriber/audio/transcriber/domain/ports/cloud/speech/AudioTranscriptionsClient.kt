package net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech

import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.Alternative

interface AudioTranscriptionsClient {
    suspend fun getAudioTranscriptionAlternatives(audioFilePath: String): List<Alternative>
}

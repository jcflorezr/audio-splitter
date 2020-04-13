package net.jcflorezr.transcriber.audio.transcriber.adapters.cloud.speech

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient

class GoogleCloudAudioTranscriptionsClient(
    private val speechApiClient: GoogleSpeechApiClient,
    private val recognitionConfig: GoogleRecognitionConfig,
    private val recognitionAudioConfig: GoogleRecognitionAudioConfig
) : AudioTranscriptionsClient {

    override suspend fun getAudioTranscriptionAlternatives(audioFilePath: String) = withContext(Dispatchers.IO) {
        speechApiClient.use { speechApiClient ->
            speechApiClient.recognize(audioFilePath, recognitionConfig, recognitionAudioConfig)
                .map { transcriptionDto -> transcriptionDto.toEntity() }
                .toList()
        }
    }
}





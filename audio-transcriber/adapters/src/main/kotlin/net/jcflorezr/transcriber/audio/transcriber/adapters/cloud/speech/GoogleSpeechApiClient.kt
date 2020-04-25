package net.jcflorezr.transcriber.audio.transcriber.adapters.cloud.speech

import com.google.cloud.speech.v1.SpeechClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.dto.GoogleCloudTranscriptionAlternativeDto
import java.util.concurrent.TimeUnit

sealed class GoogleSpeechApiClient : AutoCloseable {

    abstract suspend fun recognize(
        audioFilePath: String,
        recognitionConfig: GoogleRecognitionConfig,
        recognitionAudioConfig: GoogleRecognitionAudioConfig
    ): Sequence<GoogleCloudTranscriptionAlternativeDto>

    object SyncGoogleSpeechApiClient : GoogleSpeechApiClient() {

        private fun getSpeechApiClient(): SpeechClient = SpeechClient.create()

        override suspend fun recognize(
            audioFilePath: String,
            recognitionConfig: GoogleRecognitionConfig,
            recognitionAudioConfig: GoogleRecognitionAudioConfig
        ): Sequence<GoogleCloudTranscriptionAlternativeDto> =
            getSpeechApiClient().recognize(recognitionConfig.config, recognitionAudioConfig.getConfig(audioFilePath))
                .resultsList
                .flatMap { it.alternativesList }
                .asSequence()
                .mapIndexed { index, alternative -> GoogleCloudTranscriptionAlternativeDto.from(index, alternative) }

        override fun close() = getSpeechApiClient().close()
    }

    object AsyncGoogleSpeechApiClient : GoogleSpeechApiClient() {

        private const val RUNNING_DELAY = 3000L
        private const val RECOGNITION_TIMEOUT = 30L

        private fun getSpeechApiClient(): SpeechClient = SpeechClient.create()

        override suspend fun recognize(
            audioFilePath: String,
            recognitionConfig: GoogleRecognitionConfig,
            recognitionAudioConfig: GoogleRecognitionAudioConfig
        ): Sequence<GoogleCloudTranscriptionAlternativeDto> = withContext(Dispatchers.IO) {
            getSpeechApiClient()
                .longRunningRecognizeAsync(recognitionConfig.config, recognitionAudioConfig.getConfig(audioFilePath))
                .apply {
                    while (!isDone) { delay(RUNNING_DELAY) }
                }.get(RECOGNITION_TIMEOUT, TimeUnit.SECONDS)
                .resultsList
                .flatMap { it.alternativesList }
                .asSequence()
                .mapIndexed { index, alternative -> GoogleCloudTranscriptionAlternativeDto.from(index, alternative) }
        }

        override fun close() = getSpeechApiClient().close()
    }
}

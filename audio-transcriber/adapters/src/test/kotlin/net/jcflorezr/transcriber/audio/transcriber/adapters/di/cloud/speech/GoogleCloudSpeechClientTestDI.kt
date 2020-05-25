package net.jcflorezr.transcriber.audio.transcriber.adapters.di.cloud.speech

import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleCloudAudioTranscriptionsClient
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleRecognitionAudioConfig
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleRecognitionConfig
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleSpeechApiClient
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient
import org.mockito.Mockito.mock

class GoogleCloudSpeechClientTestDI {

    private val googleSpeechApiClientMock = mock(GoogleSpeechApiClient::class.java)
    private val googleRecognitionConfigMock = mock(GoogleRecognitionConfig::class.java)
    private val googleRecognitionAudioConfigMock = mock(GoogleRecognitionAudioConfig::class.java)

    fun googleCloudAudioTranscriptionsClientTest(): AudioTranscriptionsClient =
        GoogleCloudAudioTranscriptionsClient(
            googleSpeechApiClientMock,
            googleRecognitionConfigMock,
            googleRecognitionAudioConfigMock)

    fun googleSpeechApiClientMock() = googleSpeechApiClientMock

    fun googleRecognitionConfigMock() = googleRecognitionConfigMock

    fun googleRecognitionAudioConfigMock() = googleRecognitionAudioConfigMock
}

package net.jcflorezr.transcriber.audio.transcriber.adapters.di.cloud.speech

import net.jcflorezr.transcriber.audio.transcriber.adapters.cloud.speech.GoogleCloudAudioTranscriptionsClient
import net.jcflorezr.transcriber.audio.transcriber.adapters.cloud.speech.GoogleRecognitionAudioConfig
import net.jcflorezr.transcriber.audio.transcriber.adapters.cloud.speech.GoogleRecognitionConfig
import net.jcflorezr.transcriber.audio.transcriber.adapters.cloud.speech.GoogleSpeechApiClient
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GoogleCloudSpeechClientTestDI {

    @Bean open fun googleCloudAudioTranscriptionsClientTest(): AudioTranscriptionsClient =
        GoogleCloudAudioTranscriptionsClient(
            googleSpeechApiClientTest(),
            googleRecognitionConfigTest(),
            googleRecognitionAudioConfigTest())

    @Bean open fun googleSpeechApiClientTest(): GoogleSpeechApiClient =
        mock(GoogleSpeechApiClient::class.java)

    @Bean open fun googleRecognitionConfigTest(): GoogleRecognitionConfig =
        mock(GoogleRecognitionConfig::class.java)

    @Bean open fun googleRecognitionAudioConfigTest(): GoogleRecognitionAudioConfig =
        mock(GoogleRecognitionAudioConfig::class.java)
}

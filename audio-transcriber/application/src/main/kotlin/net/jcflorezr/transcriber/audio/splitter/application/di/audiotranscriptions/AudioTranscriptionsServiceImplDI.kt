package net.jcflorezr.transcriber.audio.splitter.application.di.audiotranscriptions

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions.AudioTranscriptionsServiceImpl
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.repositories.audiotranscriptions.DefaultAudioTranscriptionsRepositoryDI
import net.jcflorezr.transcriber.audio.transcriber.adapters.events.AudioTranscriberKafkaEventDispatcher
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleCloudAudioTranscriptionsClient
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleRecognitionAudioConfig
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleRecognitionConfig
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleSpeechApiClient
import net.jcflorezr.transcriber.audio.transcriber.adapters.repositories.audiotranscriptions.DefaultAudioTranscriptionsRepository
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.commands.GenerateAudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient
import net.jcflorezr.transcriber.core.domain.Command
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy

@ObsoleteCoroutinesApi
@Lazy
@Configuration
@Import(value = [DefaultAudioTranscriptionsRepositoryDI::class])
open class AudioTranscriptionsServiceImplDI {

    @Autowired
    private lateinit var audioTranscriptionsRepository: DefaultAudioTranscriptionsRepository

    @Bean open fun audioTranscriptionServiceImpl() =
        AudioTranscriptionsServiceImpl(audioTranscriptionsClient(), clipsDirectory(), generateAudioTranscriptionCommand())

    @Bean open fun audioTranscriptionsClient(): AudioTranscriptionsClient = getAudioTranscriptionsClient()

    // TODO: this path value must be obtained from a system variable
    @Bean open fun clipsDirectory(): String = thisClass.getResource("/temp-converted-files").path
    private val thisClass: Class<AudioTranscriptionsServiceImplDI> = this.javaClass

    @Bean open fun generateAudioTranscriptionCommand(): Command<AudioTranscription> =
        GenerateAudioTranscription(audioTranscriptionsRepository, audioTranscriberKafkaEventDispatcherTest())

    // TODO: define this variables through system variables
    private fun audioTranscriberKafkaEventDispatcherTest() =
        AudioTranscriberKafkaEventDispatcher(
            ipAddress = "localhost",
            port = 29092,
            topic = "mono-log",
            acks = 1
        )

    private fun getAudioTranscriptionsClient() =
        GoogleCloudAudioTranscriptionsClient(
            speechApiClient = GoogleSpeechApiClient.SyncGoogleSpeechApiClient,
            recognitionConfig = GoogleRecognitionConfig.ColombianSpanishWithPunctuationAndWordTimeOffsetConfig(),
            recognitionAudioConfig = GoogleRecognitionAudioConfig.GoogleCloudStorageAudioConfig
        )
}

package net.jcflorezr.transcriber.audio.splitter.application.di

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions.AudioTranscriptionDummyCommand
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions.AudioTranscriptionsServiceImpl
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient
import net.jcflorezr.transcriber.core.domain.Command
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ObsoleteCoroutinesApi
@Configuration
open class AudioTranscriptionServiceImplCpSpecDI {

    @Bean open fun audioTranscriptionServiceImplTest() =
        AudioTranscriptionsServiceImpl(audioTranscriptionsClientTest(), clipsDirectory(), audioTranscriptionDummyCommand())

    @Bean open fun audioTranscriptionsClientTest(): AudioTranscriptionsClient = mock(AudioTranscriptionsClient::class.java)

    @Bean open fun audioTranscriptionDummyCommand(): Command<AudioTranscription> = AudioTranscriptionDummyCommand()

    private val thisClass: Class<AudioTranscriptionServiceImplCpSpecDI> = this.javaClass
    @Bean open fun clipsDirectory(): String = thisClass.getResource("/temp-converted-files").path
}
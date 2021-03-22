package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audiotranscriptions

import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions.AudioTranscriptionsServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.di.events.MonoLogKafkaEventConsumerDI
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.aggregates.application.audiotranscriptions.AudioTranscriptionsService
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient
import org.mockito.Mockito.mock

@ObsoleteCoroutinesApi
object AudioTranscriptionsServiceImplCpSpecDI : CoroutineVerticle() {

    override suspend fun start() {
        vertx.deployVerticle(MonoLogKafkaEventConsumerDI)
    }

    private val clipsDirectory: String = this.javaClass.getResource("/temp-converted-files").path
    private val audioTranscriptionsMockClient: AudioTranscriptionsClient = mock(AudioTranscriptionsClient::class.java)

    private val audioTranscriptionsServiceTest: AudioTranscriptionsService =
        AudioTranscriptionsServiceImpl(
            audioTranscriptionsMockClient,
            clipsDirectory,
            GenerateAudioTranscriptionCommandDI.audioTranscriptionCommand
        )

    fun audioTranscriptionsServiceTest() = audioTranscriptionsServiceTest

    fun audioTranscriptionsMockClient() = audioTranscriptionsMockClient

    fun clipsDirectory() = clipsDirectory

    fun audioTranscriptionGeneratedEventHandler() = GenerateAudioTranscriptionCommandDI.dummyEventHandler
}

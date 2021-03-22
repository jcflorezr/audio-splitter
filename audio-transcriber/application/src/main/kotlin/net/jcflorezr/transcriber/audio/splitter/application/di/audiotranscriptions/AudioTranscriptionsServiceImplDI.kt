package net.jcflorezr.transcriber.audio.splitter.application.di.audiotranscriptions

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions.AudioTranscriptionsServiceImpl
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.audiotranscriptions.AudioTranscriptionsCassandraDaoDI
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.repositories.audiotranscriptions.DefaultAudioTranscriptionsRepositoryDI
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleCloudAudioTranscriptionsClient
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleRecognitionAudioConfig
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleRecognitionConfig
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.GoogleSpeechApiClient
import net.jcflorezr.transcriber.audio.transcriber.domain.commands.GenerateAudioTranscription
import net.jcflorezr.transcriber.core.config.broker.kafka.KafkaEventDispatcher
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraEnvProperties
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberCassandraConfig

@ObsoleteCoroutinesApi
object AudioTranscriptionsServiceImplDI {

    // Cassandra DI
    private val cassandraConfig = AudioTranscriberCassandraConfig(AudioSplitterCassandraEnvProperties.extract())
    private val cassandraDaoDI = AudioTranscriptionsCassandraDaoDI(cassandraConfig)
    private val audioTranscriptionsRepository =
        DefaultAudioTranscriptionsRepositoryDI(cassandraDaoDI).defaultAudioTranscriptionsRepository

    // Command
    private val audioTranscriberKafkaEventDispatcher =
        KafkaEventDispatcher(
            ipAddress = "localhost",
            port = 29092,
            topic = "mono-log",
            acks = 1
        )
    private val audioTranscriptionCommand =
        GenerateAudioTranscription(audioTranscriptionsRepository, audioTranscriberKafkaEventDispatcher)

    private val clipsDirectory: String = this.javaClass.getResource("/temp-converted-files").path

    private val getAudioTranscriptionsClient =
        GoogleCloudAudioTranscriptionsClient(
            speechApiClient = GoogleSpeechApiClient.SyncGoogleSpeechApiClient,
            recognitionConfig = GoogleRecognitionConfig.ColombianSpanishWithPunctuationAndWordTimeOffsetConfig(),
            recognitionAudioConfig = GoogleRecognitionAudioConfig.GoogleCloudStorageAudioConfig
        )

    fun audioTranscriptionServiceImpl() =
        AudioTranscriptionsServiceImpl(getAudioTranscriptionsClient, clipsDirectory, audioTranscriptionCommand)
}

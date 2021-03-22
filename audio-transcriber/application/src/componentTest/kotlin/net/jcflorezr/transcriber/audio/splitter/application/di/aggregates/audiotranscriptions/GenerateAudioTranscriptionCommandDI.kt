package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audiotranscriptions

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions.AudioTranscriptionGeneratedDummyHandler
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioTranscriberKafkaEventDispatcherDI
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.audiotranscriptions.AudioTranscriptionsCassandraDaoDI
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.repositories.audiotranscriptions.DefaultAudioTranscriptionsRepositoryDI
import net.jcflorezr.transcriber.audio.transcriber.domain.commands.GenerateAudioTranscription
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberComponentCassandraEnvProperties
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberComponentTestCassandraStartup

@ObsoleteCoroutinesApi
object GenerateAudioTranscriptionCommandDI {

    // Cassandra startup for component tests
    init {
        AudioTranscriptionsTablesCreation.createTablesInDb(AudioTranscriberComponentTestCassandraStartup)
    }

    // Cassandra DI
    private val cassandraConfig =
        AudioTranscriberCassandraConfig(AudioTranscriberComponentCassandraEnvProperties.extract())
    private val cassandraDaoDI = AudioTranscriptionsCassandraDaoDI(cassandraConfig)
    private val audioTranscriptionsRepository =
        DefaultAudioTranscriptionsRepositoryDI(cassandraDaoDI).defaultAudioTranscriptionsRepository

    // Command
    val audioTranscriptionCommand =
        GenerateAudioTranscription(
            audioTranscriptionsRepository,
            AudioTranscriberKafkaEventDispatcherDI.audioTranscriberTestKafkaDispatcher
        )

    // Event Handler
    val dummyEventHandler =
        AudioTranscriptionGeneratedDummyHandler(audioTranscriptionsRepository)
}

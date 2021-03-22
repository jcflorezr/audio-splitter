package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audiosegments

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments.AudioSegmentsCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments.BasicAudioSegmentsCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.di.repositories.audiosegments.DefaultAudioSegmentsRepositoryDI
import net.jcflorezr.transcriber.audio.splitter.adapters.ports.audiosegments.AudioFrameProcessorImpl
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.sourcefileinfo.DefaultSourceFileInfoRepository
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments.AudioSegmentsGeneratedDummyHandler
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventDispatcherDI
import net.jcflorezr.transcriber.audio.splitter.domain.commands.audiosegments.GenerateAudioSegments
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterComponentCassandraEnvProperties
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterComponentTestCassandraStartup
import org.mockito.Mockito

@ObsoleteCoroutinesApi
object GenerateAudioSegmentsCommandDI {

    // Cassandra startup for component tests
    init {
        AudioSegmentsTablesCreation.createTablesInDb(AudioSplitterComponentTestCassandraStartup)
    }

    // Cassandra DI
    private val cassandraConfig =
        AudioSplitterCassandraConfig(AudioSplitterComponentCassandraEnvProperties.extract())
    private val audioSegmentsDaoDI = AudioSegmentsCassandraDaoDI(cassandraConfig)
    private val basicAudioSegmentsDaoDI = BasicAudioSegmentsCassandraDaoDI(cassandraConfig)
    private val audioSegmentsRepository =
        DefaultAudioSegmentsRepositoryDI(audioSegmentsDaoDI, basicAudioSegmentsDaoDI).defaultAudioSegmentsRepository
    val sourceFileInfoRepository: DefaultSourceFileInfoRepository =
        Mockito.mock(DefaultSourceFileInfoRepository::class.java)

    private val audioSegmentsCommand =
        GenerateAudioSegments(
            sourceFileInfoRepository,
            audioSegmentsRepository,
            AudioSplitterKafkaEventDispatcherDI.audioSplitterTestKafkaDispatcher
        )

    val audioFrameProcessor = AudioFrameProcessorImpl(audioSegmentsCommand)

    // Event Handler
    val dummyEventHandler = AudioSegmentsGeneratedDummyHandler(audioSegmentsRepository)
}

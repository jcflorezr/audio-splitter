package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audioclips

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audioclips.AudioClipsCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.di.repositories.audioclips.DefaultAudioClipsRepositoryDI
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.sourcefileinfo.DefaultSourceFileInfoRepository
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipInfoGeneratedDummyHandler
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventDispatcherDI
import net.jcflorezr.transcriber.audio.splitter.domain.commands.audioclip.GenerateAudioClipInfo
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterComponentCassandraEnvProperties
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterComponentTestCassandraStartup
import org.mockito.Mockito

@ObsoleteCoroutinesApi
object GenerateAudioClipInfoCommandDI {

    // Cassandra startup for component tests
    init {
        AudioClipsTablesCreation.createTablesInDb(AudioSplitterComponentTestCassandraStartup)
    }

    // Cassandra DI
    private val cassandraConfig =
        AudioSplitterCassandraConfig(AudioSplitterComponentCassandraEnvProperties.extract())
    private val audioSegmentsDaoDI = AudioClipsCassandraDaoDI(cassandraConfig)
    private val audioClipsRepository =
        DefaultAudioClipsRepositoryDI(audioSegmentsDaoDI).defaultAudioClipsRepository
    val sourceFileInfoRepository: DefaultSourceFileInfoRepository =
        Mockito.mock(DefaultSourceFileInfoRepository::class.java)

    val audioClipInfoCommand =
        GenerateAudioClipInfo(
            audioClipsRepository,
            AudioSplitterKafkaEventDispatcherDI.audioSplitterTestKafkaDispatcher
        )

    // Event Handler
    val dummyEventHandler = AudioClipInfoGeneratedDummyHandler(audioClipsRepository)
}

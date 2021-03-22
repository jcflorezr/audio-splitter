package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.sourcefileinfo

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.sourcefileinfo.SourceFileInfoCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.di.repositories.sourcefileinfo.DefaultSourceFileInfoRepositoryDI
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo.SourceFileInfoGeneratedDummyHandler
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventDispatcherDI
import net.jcflorezr.transcriber.audio.splitter.domain.commands.sourcefileinfo.GenerateAudioSourceFileInfo
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterComponentCassandraEnvProperties
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterComponentTestCassandraStartup

@ObsoleteCoroutinesApi
object GenerateSourceFileInfoCommandDI {

    // Cassandra startup for component tests
    init {
        SourceFileInfoTablesCreation.createTablesInDb(AudioSplitterComponentTestCassandraStartup)
    }

    // Cassandra DI
    private val cassandraConfig =
        AudioSplitterCassandraConfig(AudioSplitterComponentCassandraEnvProperties.extract())
    private val cassandraDaoDI = SourceFileInfoCassandraDaoDI(cassandraConfig)
    private val sourceFileInfoRepository =
        DefaultSourceFileInfoRepositoryDI(cassandraDaoDI).defaultSourceFileInfoRepository

    // Command
    val sourceFileInfoCommand =
        GenerateAudioSourceFileInfo(
            sourceFileInfoRepository,
            AudioSplitterKafkaEventDispatcherDI.audioSplitterTestKafkaDispatcher
        )

    // Event Handler
    val dummyEventHandler = SourceFileInfoGeneratedDummyHandler(sourceFileInfoRepository)
}

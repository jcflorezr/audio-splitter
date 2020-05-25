package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audioclips

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.DataType
import com.datastax.driver.core.Session
import com.datastax.driver.core.schemabuilder.SchemaBuilder
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips.ActiveSegmentCassandraRecord
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips.AudioClipCassandraRecord
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audioclips.AudioClipsCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.di.repositories.audioclips.DefaultAudioClipsRepositoryDI
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.sourcefileinfo.DefaultSourceFileInfoRepository
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipInfoGeneratedDummyHandler
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipsInfoServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventConsumerDI
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventDispatcherDI
import net.jcflorezr.transcriber.audio.splitter.domain.commands.audioclip.GenerateAudioClipInfo
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioClipsInfoService
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterComponentTestCassandraStartup
import net.jcflorezr.transcriber.core.config.util.SystemEnvironment
import org.mockito.Mockito.mock

/*
    Service
 */

@ObsoleteCoroutinesApi
object AudioClipsServiceImplCpSpecDI : CoroutineVerticle() {

    val audioClipsServiceImpl: AudioClipsInfoService =
        GenerateAudioClipInfoCommandDI.run { AudioClipsInfoServiceImpl(sourceFileInfoRepository, audioClipInfoCommand) }

    override suspend fun start() {
        vertx.deployVerticle(AudioSplitterKafkaEventConsumerDI)
    }

    fun audioClipInfoGeneratedEventHandler() = GenerateAudioClipInfoCommandDI.dummyEventHandler

    fun sourceFileInfoRepositoryMock() = GenerateAudioClipInfoCommandDI.sourceFileInfoRepository
}

/*
    Command
 */

@ObsoleteCoroutinesApi
object GenerateAudioClipInfoCommandDI {

    // Cassandra startup for component tests
    init {
        val testCassandraStartup = AudioSplitterComponentTestCassandraStartup.apply {
            SystemEnvironment.setEnvironmentVariable(
                newEnvironment = mapOf(IP_ADDRESS_ENV_NAME to dbIpAddress(), PORT_ENV_NAME to dbPort().toString())
            )
        }
        AudioClipsTables.createTablesInDb(testCassandraStartup)
    }

    // Cassandra DI
    private val testCassandraConfig = AudioSplitterCassandraConfig
    private val audioSegmentsDaoDI = AudioClipsCassandraDaoDI(testCassandraConfig)
    private val audioClipsRepository =
        DefaultAudioClipsRepositoryDI(audioSegmentsDaoDI).defaultAudioClipsRepository
    val sourceFileInfoRepository: DefaultSourceFileInfoRepository = mock(DefaultSourceFileInfoRepository::class.java)

    val audioClipInfoCommand =
        GenerateAudioClipInfo(
            audioClipsRepository,
            AudioSplitterKafkaEventDispatcherDI.audioSplitterTestKafkaDispatcher
        )

    // Event Handler
    val dummyEventHandler = AudioClipInfoGeneratedDummyHandler(audioClipsRepository)
}

private class AudioClipsTables(private val cassandraSession: Session) {

    private val logger = KotlinLogging.logger { }

    companion object {
        fun createTablesInDb(splitterComponentConfig: AudioSplitterComponentTestCassandraStartup) {
            /*
                The statements for creating tables have to be run with Cassandra's Java Driver
                as the entire configuration must be ready before deploying the vert.x Verticles
             */
            val cassandraSession = splitterComponentConfig.run {
                Cluster.builder().addContactPoint(dbIpAddress()).withPort(dbPort()).build()
                    .connect(KEYSPACE_NAME)
            }
            val audioClipsTables = AudioClipsTables(cassandraSession)
            audioClipsTables.createAudioClipsTable()
            audioClipsTables.createActiveSegmentsTable()
        }
    }

    private fun createAudioClipsTable(): Unit = AudioClipCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(HOURS_COLUMN, DataType.cint())
            .addClusteringColumn(MINUTES_COLUMN, DataType.cint())
            .addClusteringColumn(SECONDS_COLUMN, DataType.cint())
            .addClusteringColumn(TENTHS_COLUMN, DataType.cint())
            .addColumn(DURATION_COLUMN, DataType.cfloat())
            .addColumn(CLIP_FILE_NAME_COLUMN, DataType.text())
    }.let { createTableSentence ->
        logger.info { "Creating ${AudioClipCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }

    private fun createActiveSegmentsTable(): Unit = ActiveSegmentCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(HOURS_COLUMN, DataType.cint())
            .addClusteringColumn(MINUTES_COLUMN, DataType.cint())
            .addClusteringColumn(SECONDS_COLUMN, DataType.cint())
            .addClusteringColumn(TENTHS_COLUMN, DataType.cint())
            .addClusteringColumn(SEGMENT_START_IN_SECONDS_COLUMN, DataType.cfloat())
            .addColumn(SEGMENT_END_IN_SECONDS_COLUMN, DataType.cfloat())
            .addColumn(DURATION_COLUMN, DataType.cfloat())
            .addColumn(SEGMENT_HOURS_COLUMN, DataType.cint())
            .addColumn(SEGMENT_MINUTES_COLUMN, DataType.cint())
            .addColumn(SEGMENT_SECONDS_COLUMN, DataType.cint())
            .addColumn(SEGMENT_TENTHS_COLUMN, DataType.cint())
    }.let { createTableSentence ->
        logger.info { "Creating ${ActiveSegmentCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }
}

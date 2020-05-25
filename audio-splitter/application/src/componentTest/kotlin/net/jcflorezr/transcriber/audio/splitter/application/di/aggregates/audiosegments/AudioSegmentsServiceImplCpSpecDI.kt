package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audiosegments

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.DataType
import com.datastax.driver.core.Session
import com.datastax.driver.core.schemabuilder.SchemaBuilder
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.AudioSegmentCassandraRecord
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments.AudioSegmentsCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments.BasicAudioSegmentsCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.di.repositories.audiosegments.DefaultAudioSegmentsRepositoryDI
import net.jcflorezr.transcriber.audio.splitter.adapters.ports.audiosegments.AudioFrameProcessorImpl
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.sourcefileinfo.DefaultSourceFileInfoRepository
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments.AudioSegmentsGeneratedDummyHandler
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments.AudioSegmentsServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventConsumerDI
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventDispatcherDI
import net.jcflorezr.transcriber.audio.splitter.domain.commands.audiosegments.GenerateAudioSegments
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audiosegments.application.AudioSegmentsService
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterComponentTestCassandraStartup
import net.jcflorezr.transcriber.core.config.util.SystemEnvironment
import org.mockito.Mockito.mock

/*
    Service
 */

@ObsoleteCoroutinesApi
object AudioSegmentsServiceImplCpSpecDI : CoroutineVerticle() {

    val audioSegmentsServiceImpl: AudioSegmentsService =
        AudioSegmentsServiceImpl(GenerateAudioSegmentsCommandDI.audioFrameProcessor)

    override suspend fun start() {
        vertx.deployVerticle(AudioSplitterKafkaEventConsumerDI)
    }

    fun audioSegmentsGeneratedEventHandler() = GenerateAudioSegmentsCommandDI.dummyEventHandler

    fun sourceFileInfoRepositoryMock() = GenerateAudioSegmentsCommandDI.sourceFileInfoRepository
}

/*
    Command
 */

@ObsoleteCoroutinesApi
object GenerateAudioSegmentsCommandDI {

    // Cassandra startup for component tests
    init {
        val testCassandraStartup = AudioSplitterComponentTestCassandraStartup.apply {
            SystemEnvironment.setEnvironmentVariable(
                newEnvironment = mapOf(IP_ADDRESS_ENV_NAME to dbIpAddress(), PORT_ENV_NAME to dbPort().toString())
            )
        }
        AudioSegmentsTables.createTablesInDb(testCassandraStartup)
    }

    // Cassandra DI
    private val testCassandraConfig = AudioSplitterCassandraConfig
    private val audioSegmentsDaoDI = AudioSegmentsCassandraDaoDI(testCassandraConfig)
    private val basicAudioSegmentsDaoDI = BasicAudioSegmentsCassandraDaoDI(testCassandraConfig)
    private val audioSegmentsRepository =
        DefaultAudioSegmentsRepositoryDI(audioSegmentsDaoDI, basicAudioSegmentsDaoDI).defaultAudioSegmentsRepository
    val sourceFileInfoRepository: DefaultSourceFileInfoRepository = mock(DefaultSourceFileInfoRepository::class.java)

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

private class AudioSegmentsTables(private val cassandraSession: Session) {

    private val logger = KotlinLogging.logger { }

    companion object {
        fun createTablesInDb(splitterIntegrationConfig: AudioSplitterComponentTestCassandraStartup) {
            /*
                The statements for creating tables have to be run with Cassandra's Java Driver
                as the entire configuration must be ready before deploying the vert.x Verticles
             */
            val cassandraSession = splitterIntegrationConfig.run {
                Cluster.builder().addContactPoint(dbIpAddress()).withPort(dbPort()).build()
                    .connect(KEYSPACE_NAME)
            }
            val audioSegmentsTables = AudioSegmentsTables(cassandraSession)
            audioSegmentsTables.createAudioSegmentsTable()
        }
    }

    private fun createAudioSegmentsTable(): Unit = AudioSegmentCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(SEGMENT_START_IN_SECONDS_COLUMN, DataType.cfloat())
            .addClusteringColumn(SEGMENT_END_IN_SECONDS_COLUMN, DataType.cfloat())
            .addClusteringColumn(SEGMENT_START_COLUMN, DataType.cint())
            .addClusteringColumn(SEGMENT_END_COLUMN, DataType.cint())
            .addColumn(SEGMENT_RMS_COLUMN, DataType.cdouble())
            .addColumn(SEGMENT_BYTES_COLUMN, DataType.blob())
    }.let { createTableSentence ->
        logger.info { "Creating ${AudioSegmentCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }
}

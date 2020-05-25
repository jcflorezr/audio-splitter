package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audiotranscriptions

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.DataType
import com.datastax.driver.core.Session
import com.datastax.driver.core.schemabuilder.SchemaBuilder
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions.AudioTranscriptionGeneratedDummyHandler
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions.AudioTranscriptionsServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioTranscriberKafkaEventConsumerDI
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioTranscriberKafkaEventDispatcherDI
import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.AlternativeCassandraRecord
import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.TranscriptionCassandraRecord
import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.WordCassandraRecord
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.audiotranscriptions.AudioTranscriptionsCassandraDaoDI
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.repositories.audiotranscriptions.DefaultAudioTranscriptionsRepositoryDI
import net.jcflorezr.transcriber.audio.transcriber.domain.commands.GenerateAudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.aggregates.application.audiotranscriptions.AudioTranscriptionsService
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient
import net.jcflorezr.transcriber.core.config.broker.kafka.ComponentTestKafkaStartup
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberComponentTestCassandraStartup
import net.jcflorezr.transcriber.core.config.util.SystemEnvironment
import org.mockito.Mockito.mock

/*
    Service
 */

@ObsoleteCoroutinesApi
object AudioTranscriptionsServiceImplCpSpecDI : CoroutineVerticle() {

    override suspend fun start() {
        vertx.deployVerticle(AudioTranscriberKafkaEventConsumerDI)
    }

    private val generateAudioTranscriptionCommand = GenerateAudioTranscriptionCommandDI
    private val clipsDirectory: String = this.javaClass.getResource("/temp-converted-files").path
    private val audioTranscriptionsMockClient: AudioTranscriptionsClient = mock(AudioTranscriptionsClient::class.java)

    private val audioTranscriptionsServiceTest: AudioTranscriptionsService =
        AudioTranscriptionsServiceImpl(
            audioTranscriptionsMockClient,
            clipsDirectory,
            generateAudioTranscriptionCommand.audioTranscriptionCommand
        )

    fun audioTranscriptionsServiceTest() = audioTranscriptionsServiceTest

    fun audioTranscriptionsMockClient() = audioTranscriptionsMockClient

    fun clipsDirectory() = clipsDirectory

    fun audioTranscriptionGeneratedEventHandler() = generateAudioTranscriptionCommand.dummyEventHandler
}

/*
    Command
 */

@ObsoleteCoroutinesApi
object GenerateAudioTranscriptionCommandDI {

    // Cassandra startup for component tests
    init {
        val testCassandraStartup = AudioTranscriberComponentTestCassandraStartup.apply {
            SystemEnvironment.setEnvironmentVariable(
                newEnvironment = mapOf(IP_ADDRESS_ENV_NAME to dbIpAddress(), PORT_ENV_NAME to dbPort().toString())
            )
        }
        AudioTranscriptionsTables.createTablesInDb(testCassandraStartup)
    }

    // Kafka startup for component tests
    val testKafkaStartup = ComponentTestKafkaStartup

    // Cassandra DI
    private val testCassandraConfig = AudioTranscriberCassandraConfig
    private val cassandraDaoDI = AudioTranscriptionsCassandraDaoDI(testCassandraConfig)
    val audioTranscriptionsRepository =
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

private class AudioTranscriptionsTables(private val cassandraSession: Session) {

    private val logger = KotlinLogging.logger { }

    companion object {
        fun createTablesInDb(cassandraStartup: AudioTranscriberComponentTestCassandraStartup) {
            /*
                The statements for creating tables have to be run with Cassandra's Java Driver
                as the entire configuration must be ready before deploying the vert.x Verticles
             */
            val cassandraSession = cassandraStartup.run {
                Cluster.builder().addContactPoint(dbIpAddress()).withPort(dbPort()).build()
                    .connect(KEYSPACE_NAME)
            }
            val audioTranscriptionsTables = AudioTranscriptionsTables(cassandraSession)
            audioTranscriptionsTables.createTranscriptionsTable()
            audioTranscriptionsTables.createAlternativesTable()
            audioTranscriptionsTables.createWordsTable()
        }
    }

    private fun createTranscriptionsTable(): Unit = TranscriptionCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(HOURS_COLUMN, DataType.cint())
            .addClusteringColumn(MINUTES_COLUMN, DataType.cint())
            .addClusteringColumn(SECONDS_COLUMN, DataType.cint())
            .addClusteringColumn(TENTHS_COLUMN, DataType.cint())
    }.let { createTableSentence ->
        logger.info { "Creating ${TranscriptionCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }

    private fun createAlternativesTable(): Unit = AlternativeCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(HOURS_COLUMN, DataType.cint())
            .addClusteringColumn(MINUTES_COLUMN, DataType.cint())
            .addClusteringColumn(SECONDS_COLUMN, DataType.cint())
            .addClusteringColumn(TENTHS_COLUMN, DataType.cint())
            .addClusteringColumn(ALTERNATIVE_POSITION_COLUMN, DataType.cint())
            .addColumn(TRANSCRIPTION_COLUMN, DataType.text())
            .addColumn(CONFIDENCE_COLUMN, DataType.cfloat())
    }.let { createTableSentence ->
        logger.info { "Creating ${AlternativeCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }

    private fun createWordsTable(): Unit = WordCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addClusteringColumn(HOURS_COLUMN, DataType.cint())
            .addClusteringColumn(MINUTES_COLUMN, DataType.cint())
            .addClusteringColumn(SECONDS_COLUMN, DataType.cint())
            .addClusteringColumn(TENTHS_COLUMN, DataType.cint())
            .addClusteringColumn(ALTERNATIVE_POSITION_COLUMN, DataType.cint())
            .addClusteringColumn(ALTERNATIVE_WORD_POSITION_COLUMN, DataType.cint())
            .addColumn(WORD_COLUMN, DataType.text())
            .addColumn(FROM_TIME_COLUMN, DataType.cfloat())
            .addColumn(TO_TIME_COLUMN, DataType.cfloat())
    }.let { createTableSentence ->
        logger.info { "Creating ${WordCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }
}

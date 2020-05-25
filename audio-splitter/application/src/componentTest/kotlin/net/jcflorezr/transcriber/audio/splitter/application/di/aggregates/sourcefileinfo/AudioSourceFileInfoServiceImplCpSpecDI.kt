package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.sourcefileinfo

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.DataType
import com.datastax.driver.core.Session
import com.datastax.driver.core.schemabuilder.SchemaBuilder
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo.SourceFileContentInfoCassandraRecord
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo.SourceFileMetadataCassandraRecord
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.sourcefileinfo.SourceFileInfoCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.di.repositories.sourcefileinfo.DefaultSourceFileInfoRepositoryDI
import net.jcflorezr.transcriber.audio.splitter.adapters.ports.sourcefileinfo.JAudioTaggerMetadataGenerator
import net.jcflorezr.transcriber.audio.splitter.adapters.ports.sourcefileinfo.JavaAudioWavConverter
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo.AudioSourceFileInfoServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo.SourceFileInfoGeneratedDummyHandler
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventConsumerDI
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventDispatcherDI
import net.jcflorezr.transcriber.audio.splitter.domain.commands.sourcefileinfo.GenerateAudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.application.AudioSourceFileInfoService
import net.jcflorezr.transcriber.audio.splitter.domain.ports.cloud.storage.CloudStorageClient
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterComponentTestCassandraStartup
import net.jcflorezr.transcriber.core.config.util.SystemEnvironment
import org.mockito.Mockito.mock

/*
    Service
 */

@ObsoleteCoroutinesApi
object AudioSourceFileInfoServiceImplCpSpecDI : CoroutineVerticle() {

    val googleCloudStorageClientTest: CloudStorageClient = mock(CloudStorageClient::class.java)
    private val jAudioTaggerMetadataGenerator = JAudioTaggerMetadataGenerator()
    private val generateAudioFileInfoCommand = GenerateSourceFileInfoCommandDI.sourceFileInfoCommand
    private val tempLocalDirectory: String = this.javaClass.getResource("/temp-converted-files/source-file-info").path
    private val javaAudioWavConverterTest = JavaAudioWavConverter(tempLocalDirectory)

    val audioSourceFileInfoServiceTest: AudioSourceFileInfoService =
        AudioSourceFileInfoServiceImpl(
            storageClient = googleCloudStorageClientTest,
            audioWavConverter = javaAudioWavConverterTest,
            audioFileMetadataGenerator = jAudioTaggerMetadataGenerator,
            command = generateAudioFileInfoCommand
        )

    override suspend fun start() {
        vertx.deployVerticle(AudioSplitterKafkaEventConsumerDI)
    }

    fun sourceFileInfoGeneratedEventHandler() = GenerateSourceFileInfoCommandDI.dummyEventHandler
}

/*
    Command
 */

@ObsoleteCoroutinesApi
object GenerateSourceFileInfoCommandDI {

    // Cassandra startup for component tests
    init {
        val testCassandraStartup = AudioSplitterComponentTestCassandraStartup.apply {
            SystemEnvironment.setEnvironmentVariable(
                newEnvironment = mapOf(IP_ADDRESS_ENV_NAME to dbIpAddress(), PORT_ENV_NAME to dbPort().toString())
            )
        }
        SourceFileInfoTables.createTablesInDb(testCassandraStartup)
    }

    // Cassandra DI
    private val testCassandraConfig = AudioSplitterCassandraConfig
    private val cassandraDaoDI = SourceFileInfoCassandraDaoDI(testCassandraConfig)
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

private class SourceFileInfoTables(private val cassandraSession: Session) {

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
            val sourceFileInfoTables = SourceFileInfoTables(cassandraSession)
            sourceFileInfoTables.createSourceFileMetadataTable()
            sourceFileInfoTables.createSourceFileContentInfoTable()
        }
    }

    private fun createSourceFileMetadataTable(): Unit = SourceFileMetadataCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addColumn(TITLE_COLUMN, DataType.text())
            .addColumn(ALBUM_COLUMN, DataType.text())
            .addColumn(ARTIST_COLUMN, DataType.text())
            .addColumn(TRACK_NUMBER_COLUMN, DataType.text())
            .addColumn(GENRE_COLUMN, DataType.text())
            .addColumn(DURATION_COLUMN, DataType.cint())
            .addColumn(SAMPLE_RATE_COLUMN, DataType.text())
            .addColumn(CHANNELS_COLUMN, DataType.text())
            .addColumn(COMMENTS_COLUMN, DataType.text())
    }.let { createTableSentence ->
        logger.info { "Creating ${SourceFileMetadataCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }

    private fun createSourceFileContentInfoTable(): Unit = SourceFileContentInfoCassandraRecord.run {
        SchemaBuilder.createTable(TABLE_NAME)
            .ifNotExists()
            .addPartitionKey(AUDIO_FILE_NAME_COLUMN, DataType.text())
            .addColumn(CHANNELS_COLUMN, DataType.cint())
            .addColumn(SAMPLE_RATE_COLUMN, DataType.cint())
            .addColumn(SAMPLE_SIZE_IN_BITS_COLUMN, DataType.cint())
            .addColumn(FRAME_SIZE_COLUMN, DataType.cint())
            .addColumn(SAMPLE_SIZE_COLUMN, DataType.cint())
            .addColumn(BIG_ENDIAN_COLUMN, DataType.cboolean())
            .addColumn(ENCODING_COLUMN, DataType.text())
            .addColumn(TOTAL_FRAMES_COLUMN, DataType.cint())
            .addColumn(EXACT_TOTAL_FRAMES_COLUMN, DataType.cint())
            .addColumn(TOTAL_FRAMES_BY_SECOND_COLUMN, DataType.cint())
            .addColumn(REMAINING_FRAMES_COLUMN, DataType.cint())
            .addColumn(FRAMES_PER_SECOND_COLUMN, DataType.cint())
            .addColumn(NUM_OF_AUDIO_SEGMENTS_COLUMN, DataType.cint())
            .addColumn(AUDIO_SEGMENT_LENGTH_COLUMN, DataType.cint())
            .addColumn(AUDIO_SEGMENT_LENGTH_IN_BYTES_COLUMN, DataType.cint())
            .addColumn(AUDIO_SEGMENTS_PER_SECOND_COLUMN, DataType.cint())
            .addColumn(REMAINING_AUDIO_SEGMENTS_COLUMN, DataType.cint())
    }.let { createTableSentence ->
        logger.info { "Creating ${SourceFileContentInfoCassandraRecord.TABLE_NAME} table: $createTableSentence" }
        cassandraSession.execute(createTableSentence.toString())
    }
}

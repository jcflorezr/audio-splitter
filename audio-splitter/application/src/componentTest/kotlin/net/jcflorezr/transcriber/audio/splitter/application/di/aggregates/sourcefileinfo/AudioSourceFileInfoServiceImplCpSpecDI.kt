package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.sourcefileinfo

import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.adapters.ports.sourcefileinfo.JAudioTaggerMetadataGenerator
import net.jcflorezr.transcriber.audio.splitter.adapters.ports.sourcefileinfo.JavaAudioWavConverter
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo.AudioSourceFileInfoServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventConsumerDI
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.application.AudioSourceFileInfoService
import net.jcflorezr.transcriber.audio.splitter.domain.ports.cloud.storage.CloudStorageClient
import org.mockito.Mockito.mock

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

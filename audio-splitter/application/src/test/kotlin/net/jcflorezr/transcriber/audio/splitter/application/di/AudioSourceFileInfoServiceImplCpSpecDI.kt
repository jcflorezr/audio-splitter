package net.jcflorezr.transcriber.audio.splitter.application.di

import net.jcflorezr.transcriber.audio.splitter.adapters.sourcefileinfo.JAudioTaggerMetadataGenerator
import net.jcflorezr.transcriber.audio.splitter.adapters.sourcefileinfo.JavaAudioWavConverter
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo.AudioSourceFileInfoDummyCommand
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo.AudioSourceFileInfoService
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo.AudioSourceFileInfoServiceImpl
import net.jcflorezr.transcriber.audio.splitter.domain.ports.cloud.storage.CloudStorageClient
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.AudioFileMetadataGenerator
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.AudioWavConverter
import net.jcflorezr.transcriber.core.domain.Command
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AudioSourceFileInfoServiceImplCpSpecDI {

    @Bean open fun audioSourceFileInfoServiceTest(): AudioSourceFileInfoService =
        AudioSourceFileInfoServiceImpl(
            storageClient = googleCloudStorageClientTest(),
            audioWavConverter = javaAudioWavConverterTest(),
            audioFileMetadataGenerator = jAudioTaggerMetadataGenerator(),
            command = generateAudioFileInfoCommand())

    // CloudStorageClient

    @Bean open fun googleCloudStorageClientTest(): CloudStorageClient = mock(CloudStorageClient::class.java)

    // AudioWavConverter

    @Bean open fun javaAudioWavConverterTest(): AudioWavConverter =
        JavaAudioWavConverter(tempLocalDirectory = tempLocalDirectory())

    private val thisClass: Class<AudioSourceFileInfoServiceImplCpSpecDI> = this.javaClass
    private fun tempLocalDirectory() = thisClass.getResource("/temp-converted-files").path

    // AudioFileMetadataGenerator

    @Bean open fun jAudioTaggerMetadataGenerator(): AudioFileMetadataGenerator =
        JAudioTaggerMetadataGenerator()

    // Command
    private fun generateAudioFileInfoCommand(): Command = AudioSourceFileInfoDummyCommand()
}
package net.jcflorezr.audio.splitter.application.di

import net.jcflorezr.audio.splitter.adapters.sourcefile.JAudioTaggerMetadataGenerator
import net.jcflorezr.audio.splitter.adapters.sourcefile.JavaAudioWavConverter
import net.jcflorezr.audio.splitter.application.sourcefile.AudioSourceFileInfoService
import net.jcflorezr.audio.splitter.application.sourcefile.AudioSourceFileInfoServiceImpl
import net.jcflorezr.audio.splitter.domain.cloud.storage.CloudStorageClient
import net.jcflorezr.audio.splitter.domain.sourcefile.AudioFileMetadataGenerator
import net.jcflorezr.audio.splitter.domain.sourcefile.AudioWavConverter
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AudioSourceFileInfoServiceImplCpSpecDI {

    @Bean open fun audioSourceFileInfoServiceTest(): AudioSourceFileInfoService =
        AudioSourceFileInfoServiceImpl(
            cloudStorageClient = googleCloudStorageClientTest(),
            audioWavConverter = javaAudioWavConverterTest(),
            audioFileMetadataGenerator = jAudioTaggerMetadataGenerator())

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
}
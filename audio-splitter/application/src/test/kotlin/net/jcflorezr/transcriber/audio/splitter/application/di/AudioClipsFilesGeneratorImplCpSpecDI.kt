package net.jcflorezr.transcriber.audio.splitter.application.di

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipsFilesGenerator
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipsFilesGeneratorDummyCommand
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipsFilesGeneratorImpl
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.AudioSegmentsRepository
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ObsoleteCoroutinesApi
@Configuration
open class AudioClipsFilesGeneratorImplCpSpecDI {

    @Bean open fun audioClipsFilesGeneratorTest(): AudioClipsFilesGenerator =
        AudioClipsFilesGeneratorImpl(
            audioClipsFilesGeneratorDummyCommand(),
            audioSegmentsRepositoryTest(),
            sourceFileInfoRepositoryTest(),
            tempLocalDirectory())

    @Bean open fun audioSegmentsRepositoryTest(): AudioSegmentsRepository = mock(AudioSegmentsRepository::class.java)

    @Bean open fun sourceFileInfoRepositoryTest(): SourceFileInfoRepository = mock(SourceFileInfoRepository::class.java)

    @Bean open fun audioClipsFilesGeneratorDummyCommand() =
        AudioClipsFilesGeneratorDummyCommand(tempLocalDirectory())

    private val thisClass: Class<AudioClipsFilesGeneratorImplCpSpecDI> = this.javaClass
    private fun tempLocalDirectory() = thisClass.getResource("/temp-converted-files").path
}
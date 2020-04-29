package net.jcflorezr.transcriber.audio.splitter.application.di

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipsFilesGeneratorImpl
import net.jcflorezr.transcriber.audio.splitter.application.commands.audioclips.AudioClipsFilesGeneratorDummyCommand
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioClipsFilesGenerator
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.audiosegments.AudioSegmentsRepository
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@ObsoleteCoroutinesApi
@Configuration
@Lazy
open class AudioClipsFilesGeneratorImplCpSpecDI {

    @Bean open fun audioClipsFilesGeneratorTest(): AudioClipsFilesGenerator =
        AudioClipsFilesGeneratorImpl(
            audioSegmentsRepositoryTest(),
            sourceFileInfoRepositoryTest(),
            tempLocalDirectory(),
            audioClipsFilesGeneratorDummyCommand())

    @Bean open fun audioSegmentsRepositoryTest(): AudioSegmentsRepository = mock(AudioSegmentsRepository::class.java)

    @Bean open fun sourceFileInfoRepositoryTest(): SourceFileInfoRepository = mock(SourceFileInfoRepository::class.java)

    @Bean open fun audioClipsFilesGeneratorDummyCommand() = AudioClipsFilesGeneratorDummyCommand(tempLocalDirectory())

    private val thisClass: Class<AudioClipsFilesGeneratorImplCpSpecDI> = this.javaClass
    private fun tempLocalDirectory() = thisClass.getResource("/temp-converted-files/audio-clips").path
}

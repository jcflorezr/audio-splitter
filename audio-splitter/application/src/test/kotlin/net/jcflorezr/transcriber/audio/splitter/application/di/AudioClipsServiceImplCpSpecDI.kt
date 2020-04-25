package net.jcflorezr.transcriber.audio.splitter.application.di

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.commands.audioclips.AudioClipsDummyCommand
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipsInfoServiceImpl
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioClipsInfoService
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ObsoleteCoroutinesApi
@Configuration
open class AudioClipsServiceImplCpSpecDI {

    @Bean open fun audioClipsInfoServiceTest(): AudioClipsInfoService =
        AudioClipsInfoServiceImpl(sourceFileInfoRepositoryTest(), audioClipsDummyCommand())

    @Bean open fun sourceFileInfoRepositoryTest(): SourceFileInfoRepository = mock(SourceFileInfoRepository::class.java)

    @Bean open fun audioClipsDummyCommand() = AudioClipsDummyCommand()
}
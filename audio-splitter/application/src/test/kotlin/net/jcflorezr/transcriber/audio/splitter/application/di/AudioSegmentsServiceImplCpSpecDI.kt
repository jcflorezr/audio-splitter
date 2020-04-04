package net.jcflorezr.transcriber.audio.splitter.application.di

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments.AudioSegmentsService
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments.AudioSegmentsServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments.AudioSegmentsDummyCommand
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ObsoleteCoroutinesApi
@Configuration
open class AudioSegmentsServiceImplCpSpecDI {

    @Bean open fun audioPartsServiceTest(): AudioSegmentsService =
        AudioSegmentsServiceImpl(audioSegmentsDummyCommand())

    @Bean open fun audioSegmentsDummyCommand() = AudioSegmentsDummyCommand()
}
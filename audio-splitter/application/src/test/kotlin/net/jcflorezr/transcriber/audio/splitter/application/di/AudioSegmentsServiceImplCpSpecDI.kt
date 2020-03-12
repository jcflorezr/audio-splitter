package net.jcflorezr.transcriber.audio.splitter.application.di

import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments.AudioSegmentsService
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments.AudioSegmentsServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.audiosegments.AudioSegmentsDummyCommand
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AudioSegmentsServiceImplCpSpecDI {

    @Bean open fun audioPartsServiceTest(): AudioSegmentsService =
        AudioSegmentsServiceImpl(audioSegmentsDummyCommand())

    @Bean open fun audioSegmentsDummyCommand() = AudioSegmentsDummyCommand()
}
package net.jcflorezr.transcriber.audio.splitter.application.di

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments.AudioSegmentsServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.commands.audiosegments.AudioSegmentsDummyCommand
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audiosegments.application.AudioSegmentsService
import net.jcflorezr.transcriber.core.domain.Command
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ObsoleteCoroutinesApi
@Configuration
open class AudioSegmentsServiceImplCpSpecDI {

    @Bean open fun audioPartsServiceTest(): AudioSegmentsService =
        AudioSegmentsServiceImpl(audioSegmentsDummyCommand())

    @Bean open fun audioSegmentsDummyCommand(): Command<AudioSegment> = AudioSegmentsDummyCommand()
}
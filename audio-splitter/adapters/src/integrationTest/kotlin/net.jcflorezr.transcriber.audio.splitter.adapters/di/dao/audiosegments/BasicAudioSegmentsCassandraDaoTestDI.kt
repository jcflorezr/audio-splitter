package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments

import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.AudioSegmentsCassandraDao
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.BasicAudioSegmentsCassandraDao
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.AudioSplitterCassandraDaoTestDI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@Lazy
open class BasicAudioSegmentsCassandraDaoTestDI : AudioSplitterCassandraDaoTestDI() {

    @Bean
    open fun basicAudioSplitterCassandraDaoTest(): BasicAudioSegmentsCassandraDao =
        BasicAudioSegmentsCassandraDao(cassandraOperations)

    @Bean
    open fun audioSplitterCassandraDaoTest(): AudioSegmentsCassandraDao =
        AudioSegmentsCassandraDao(cassandraOperations)
}

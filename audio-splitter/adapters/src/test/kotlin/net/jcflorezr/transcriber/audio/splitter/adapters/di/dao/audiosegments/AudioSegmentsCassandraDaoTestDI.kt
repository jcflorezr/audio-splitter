package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments

import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.AudioSegmentsCassandraDao
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.AudioSplitterCassandraDaoTestDI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AudioSegmentsCassandraDaoTestDI : AudioSplitterCassandraDaoTestDI() {

    @Bean
    open fun audioSplitterCassandraDaoTest(): AudioSegmentsCassandraDao =
        AudioSegmentsCassandraDao(cassandraOperations)
}
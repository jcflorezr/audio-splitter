package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audioclips

import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips.AudioClipsCassandraDao
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.AudioSplitterCassandraDaoTestDI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AudioClipsCassandraDaoTestDI : AudioSplitterCassandraDaoTestDI() {

    @Bean
    open fun audioClipsCassandraDaoTest(): AudioClipsCassandraDao =
        AudioClipsCassandraDao(cassandraOperations)
}

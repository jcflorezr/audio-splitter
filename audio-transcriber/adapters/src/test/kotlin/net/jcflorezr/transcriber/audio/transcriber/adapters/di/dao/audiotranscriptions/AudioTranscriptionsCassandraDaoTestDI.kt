package net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.audiotranscriptions

import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.AudioTranscriptionsCassandraDao
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.AudioTranscriberCassandraDaoTestDI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AudioTranscriptionsCassandraDaoTestDI : AudioTranscriberCassandraDaoTestDI() {

    @Bean
    open fun audioTranscriptionsCassandraDaoTest(): AudioTranscriptionsCassandraDao =
        AudioTranscriptionsCassandraDao(cassandraOperations)
}
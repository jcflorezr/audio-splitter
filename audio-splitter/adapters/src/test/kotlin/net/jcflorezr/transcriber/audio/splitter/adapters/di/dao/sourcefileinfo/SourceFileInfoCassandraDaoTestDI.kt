package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo.SourceFileInfoCassandraDao
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.AudioSplitterCassandraDaoTestDI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SourceFileInfoCassandraDaoTestDI : AudioSplitterCassandraDaoTestDI() {

    @Bean
    open fun sourceFileInfoCassandraDaoTest(): SourceFileInfoCassandraDao =
        SourceFileInfoCassandraDao(cassandraOperations)
}

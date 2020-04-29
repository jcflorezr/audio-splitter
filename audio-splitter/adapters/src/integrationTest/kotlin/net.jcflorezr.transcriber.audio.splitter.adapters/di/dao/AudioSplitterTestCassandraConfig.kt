package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao

import java.io.File
import java.nio.file.Files
import net.jcflorezr.transcriber.core.config.TestCassandraConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean
import org.springframework.data.cassandra.core.CassandraAdminOperations
import org.springframework.data.cassandra.core.CassandraAdminTemplate
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.CassandraTemplate

@Configuration
@Lazy
open class AudioSplitterTestCassandraConfig : TestCassandraConfig() {

    private val thisClass: Class<AudioSplitterTestCassandraConfig> = this.javaClass
    private val startUpScriptsFilePath: String

    init {
        startUpScriptsFilePath = thisClass.getResource("/cassandra").path
    }

    override fun getKeyspaceName(): String = "AUDIO_SPLITTER"

    override fun getStartupScripts(): List<String> =
        Files.readAllLines(
            File("$startUpScriptsFilePath/audio-splitter-database-init-script.cql").toPath(),
            Charsets.UTF_8)

    @Bean("audioSplitterTestCassandraSession")
    override fun session(): CassandraSessionFactoryBean {
        val session: CassandraSessionFactoryBean = super.session()
        session.setKeyspaceName(keyspaceName)
        return session
    }

    @Bean
    open fun audioSplitterTestCassandraTemplate(
        @Qualifier("audioSplitterTestCassandraSession") session: CassandraSessionFactoryBean
    ): CassandraOperations {
        return CassandraTemplate(session.getObject())
    }

    @Bean
    open fun audioSplitterTestCassandraAdminTemplate(
        @Qualifier("audioSplitterTestCassandraSession") session: CassandraSessionFactoryBean
    ): CassandraAdminOperations {
        return CassandraAdminTemplate(session.getObject(), cassandraConverter())
    }
}

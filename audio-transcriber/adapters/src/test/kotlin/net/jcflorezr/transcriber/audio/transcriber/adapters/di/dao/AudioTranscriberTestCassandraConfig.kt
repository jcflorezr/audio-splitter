package net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao

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
open class AudioTranscriberTestCassandraConfig : TestCassandraConfig() {

    private val thisClass: Class<AudioTranscriberTestCassandraConfig> = this.javaClass
    private val startUpScriptsFilePath: String

    init {
        startUpScriptsFilePath = thisClass.getResource("/cassandra").path
    }

    override fun getKeyspaceName(): String = "AUDIO_TRANSCRIBER"

    override fun getStartupScripts(): List<String> =
        Files.readAllLines(
            File("$startUpScriptsFilePath/audio-transcriber-database-init-script.cql").toPath(), Charsets.UTF_8)

    @Bean("audioTranscriberTestCassandraSession")
    override fun session(): CassandraSessionFactoryBean {
        val session: CassandraSessionFactoryBean = super.session()
        session.setKeyspaceName(keyspaceName)
        return session
    }

    @Bean
    open fun audioTranscriberTestCassandraTemplate(
        @Qualifier("audioTranscriberTestCassandraSession") session: CassandraSessionFactoryBean
    ): CassandraOperations {
        return CassandraTemplate(session.getObject())
    }

    @Bean
    open fun audioTranscriberTestCassandraAdminTemplate(
        @Qualifier("audioTranscriberTestCassandraSession") session: CassandraSessionFactoryBean
    ): CassandraAdminOperations {
        return CassandraAdminTemplate(session.getObject(), cassandraConverter())
    }
}

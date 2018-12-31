package net.jcflorezr.config

import net.jcflorezr.dao.SourceFileDao
import net.jcflorezr.dao.SourceFileDaoImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.CassandraTemplate

@Configuration
@PropertySource(value = ["classpath:config/cassandra.properties"])
class CassandraConfig : AbstractCassandraConfiguration() {

    @Value("\${cassandra.contactpoints}")
    private lateinit var contactPoints: String
    @Value("\${cassandra.port}")
    private lateinit var port: Integer
    @Value("\${cassandra.keyspace}")
    private lateinit var keySpace: String
    @Value("\${cassandra.base-package}")
    private lateinit var basePackage: String
    @Value("\${cassandra.keyspace.creation.script}")
    private lateinit var keyspaceCreationScript: String
    @Value("\${cassandra.keyspace.activation.script}")
    private lateinit var keyspaceActivationScript: String

    override fun getPort() = port.toInt()

    override fun getContactPoints() = contactPoints

    override fun getKeyspaceName() = keySpace

    override fun getEntityBasePackages() = arrayOf(basePackage)

    override fun getStartupScripts() =
        mutableListOf(keyspaceCreationScript, keyspaceActivationScript)

    @Bean fun cassandraCustomTemplate(): CassandraOperations = CassandraTemplate(session().getObject())

    /*
    DAOs
     */

    @Bean fun sourceFileDao(): SourceFileDao = SourceFileDaoImpl()

}

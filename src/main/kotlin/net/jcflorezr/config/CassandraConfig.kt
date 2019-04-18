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
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean
import org.springframework.data.cassandra.config.SchemaAction
import org.springframework.data.cassandra.core.CassandraAdminTemplate
import org.springframework.data.cassandra.core.convert.CassandraConverter
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver

@Configuration
@PropertySource(value = ["classpath:config/cassandra.properties"])
class CassandraConfig : AbstractCassandraConfiguration() {

    @Value("\${cassandra.contactpoints}")
    private lateinit var contactPoint: String
    @Value("\${cassandra.port}")
    private lateinit var port: Integer
    @Value("\${cassandra.keyspace}")
    private lateinit var keySpace: String
    @Value("\${cassandra.keyspace.activation.script}")
    private lateinit var keyspaceActivationScript: String

    override fun getKeyspaceName() = keySpace

    @Bean
    override fun cluster(): CassandraClusterFactoryBean {
        val cluster = CassandraClusterFactoryBean()
        cluster.setContactPoints(contactPoint)
        cluster.setPort(port.toInt())
        cluster.afterPropertiesSet()
        return cluster
    }

    @Bean
    fun mappingContext(): CassandraMappingContext {
        val mappingContext = CassandraMappingContext()
        mappingContext.setUserTypeResolver(SimpleUserTypeResolver(cluster().getObject(), keyspaceName))
        return mappingContext
    }

    @Bean fun converter(): CassandraConverter = MappingCassandraConverter(mappingContext())

    @Bean
    override fun session(): CassandraSessionFactoryBean {
        val session = CassandraSessionFactoryBean()
        session.setCluster(cluster().getObject())
        session.setKeyspaceName(keyspaceName)
        session.setConverter(converter())
        session.schemaAction = SchemaAction.NONE
        session.afterPropertiesSet()
        return session
    }

    @Bean override fun cassandraMapping(): CassandraMappingContext = CassandraMappingContext()

    @Bean
    fun cassandraCustomTemplate(): CassandraOperations {
        return CassandraTemplate(session().getObject())
    }

    @Bean
    fun cassandraAdminTemplate(): CassandraOperations {
        return CassandraAdminTemplate(session().getObject(), converter())
    }

    /*
    DAOs
     */

    @Bean fun sourceFileDao(): SourceFileDao = SourceFileDaoImpl()
}

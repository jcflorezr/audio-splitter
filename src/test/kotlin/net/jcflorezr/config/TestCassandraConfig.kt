package net.jcflorezr.config

import net.jcflorezr.persistence.SourceFileDao
import net.jcflorezr.persistence.SourceFileDaoImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean
import org.springframework.data.cassandra.config.SchemaAction
import org.springframework.data.cassandra.core.CassandraAdminTemplate
import org.springframework.data.cassandra.core.CassandraOperations
import org.springframework.data.cassandra.core.CassandraTemplate
import org.springframework.data.cassandra.core.convert.CassandraConverter
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter
import org.springframework.data.cassandra.core.mapping.BasicCassandraMappingContext
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver


@Configuration
@PropertySource(value = ["classpath:config/test-cassandra.properties"])
class TestCassandraConfig : AbstractCassandraConfiguration() {

    @Value("\${cassandra.contactpoints}")
    private lateinit var contactPoints: String
    @Value("\${cassandra.port}")
    private lateinit var port: Integer
    @Value("\${cassandra.keyspace}")
    private lateinit var keySpace: String

    override fun getPort() = port.toInt()

    override fun getContactPoints() = contactPoints

    override fun getKeyspaceName() = keySpace

    @Profile("test")
    @Bean
    override fun cluster(): CassandraClusterFactoryBean {
        val cluster = CassandraClusterFactoryBean()
        cluster.setContactPoints(getContactPoints())
        cluster.setPort(getPort())
        cluster.afterPropertiesSet()
        return cluster
    }

    @Profile("test")
    @Bean
    fun mappingContext(): CassandraMappingContext {
        val mappingContext = BasicCassandraMappingContext()
        mappingContext.setUserTypeResolver(SimpleUserTypeResolver(cluster().getObject(), keyspaceName))
        return mappingContext
    }

    @Profile("test")
    @Bean
    fun converter(): CassandraConverter = MappingCassandraConverter(mappingContext())

    @Profile("test")
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

    @Profile("test") @Bean override fun cassandraMapping(): CassandraMappingContext = BasicCassandraMappingContext()

    @Profile("test")
    @Bean
    fun cassandraCustomTemplate(): CassandraOperations {
        return CassandraTemplate(session().getObject())
    }

    @Profile("test")
    @Bean
    fun cassandraAdminTemplate(): CassandraOperations {
        return CassandraAdminTemplate(session().getObject(), converter())
    }

    /*
    DAOs
     */

    @Profile("test") @Bean fun sourceFileDao(): SourceFileDao = SourceFileDaoImpl()

}

package net.jcflorezr.config

import net.jcflorezr.dao.CassandraInitializer
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
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext
import org.springframework.data.cassandra.core.mapping.SimpleUserTypeResolver

@Configuration
@PropertySource(value = ["classpath:config/test-cassandra.properties"])
class TestCassandraConfig : AbstractCassandraConfiguration() {

    @Value("\${cassandra.keyspace}")
    private lateinit var keySpace: String

    override fun getKeyspaceName() = keySpace

    @Profile("test")
    @Bean
    override fun cluster(): CassandraClusterFactoryBean {
        val cassandraDockerContainer = CassandraInitializer.cassandraDockerContainer
        val cluster = CassandraClusterFactoryBean()
        cluster.setContactPoints(cassandraDockerContainer.containerIpAddress)
        cluster.setPort(cassandraDockerContainer.getMappedPort(CassandraInitializer.cassandraPort))
        cluster.afterPropertiesSet()
        return cluster
    }

    @Profile("test")
    @Bean
    fun mappingContextTest(): CassandraMappingContext {
        val mappingContext = CassandraMappingContext()
        mappingContext.setUserTypeResolver(SimpleUserTypeResolver(cluster().getObject(), keyspaceName))
        return mappingContext
    }

    @Profile("test") @Bean fun converterTest(): CassandraConverter = MappingCassandraConverter(mappingContextTest())

    @Profile("test")
    @Bean
    override fun session(): CassandraSessionFactoryBean {
        val session = CassandraSessionFactoryBean()
        session.setCluster(cluster().getObject())
        session.setKeyspaceName(keyspaceName)
        session.setConverter(converterTest())
        session.schemaAction = SchemaAction.NONE
        session.afterPropertiesSet()
        return session
    }

    @Profile("test") @Bean override fun cassandraMapping(): CassandraMappingContext = CassandraMappingContext()

    @Profile("test")
    @Bean
    fun cassandraCustomTemplateTest(): CassandraOperations {
        return CassandraTemplate(session().getObject())
    }

    @Profile("test")
    @Bean
    fun cassandraAdminTemplateTest(): CassandraOperations {
        return CassandraAdminTemplate(session().getObject(), converterTest())
    }

}

package net.jcflorezr.transcriber.core.config

import org.springframework.data.cassandra.config.AbstractCassandraConfiguration
import org.springframework.data.cassandra.config.SchemaAction
import org.testcontainers.containers.CassandraContainer

abstract class TestCassandraConfig : AbstractCassandraConfiguration() {

    companion object {
        private val CASSANDRA_CONTAINER: TestCassandraContainer = TestCassandraContainer("cassandra:3.11.2")
        init {
            CASSANDRA_CONTAINER.start()
        }
    }

    override fun getContactPoints(): String = CASSANDRA_CONTAINER.containerIpAddress

    override fun getPort(): Int = CASSANDRA_CONTAINER.firstMappedPort

    override fun getSchemaAction(): SchemaAction = SchemaAction.CREATE_IF_NOT_EXISTS
}

class TestCassandraContainer(
    imageName: String
) : CassandraContainer<TestCassandraContainer>(imageName)

package net.jcflorezr.transcriber.core.config.db.cassandra

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

object AudioSplitterComponentCassandraEnvProperties {

    fun extract() = CassandraConfigProperties.createNew(extractEnvironmentProperties())

    private fun extractEnvironmentProperties(): ObjectNode =
        ObjectMapper().createObjectNode()
            .put("keyspace-name", System.getProperty("TRANSCRIBER_COMPONENT_CASSANDRA_DB_KEYSPACE_NAME"))
            .put("ip-address", System.getProperty("TRANSCRIBER_COMPONENT_CASSANDRA_DB_IP_ADDRESS"))
            .put("port", System.getProperty("TRANSCRIBER_COMPONENT_CASSANDRA_DB_PORT"))
}

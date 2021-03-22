package net.jcflorezr.transcriber.core.config.db.cassandra

import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.lang.IllegalArgumentException

data class CassandraConfigProperties(
    val keyspaceName: String,
    val ipAddress: String,
    val port: Int
) {

    companion object {
        fun createNew(envVars: ObjectNode): CassandraConfigProperties {
            return CassandraConfigProperties(
                keyspaceName = envVars.get("keyspace-name").takeIf { it !is NullNode }?.asText() ?: throw missingEnvPropertyException("keyspaceName"),
                ipAddress = envVars.get("ip-address").takeIf { it !is NullNode }?.asText() ?: throw missingEnvPropertyException("ipAddress"),
                port = envVars.get("port").takeIf { it !is NullNode }?.asInt() ?: throw missingEnvPropertyException("port")
            )
        }

        private fun missingEnvPropertyException(envVariableName: String) =
            IllegalArgumentException("No value was set for '$envVariableName' env variable")
    }
}

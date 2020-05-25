package net.jcflorezr.transcriber.core.config.db.cassandra

import io.vertx.cassandra.CassandraClientOptions
import java.lang.IllegalArgumentException

interface CassandraConfig {

    companion object {
        private const val IP_ADDRESS_ENV_NAME = "TRANSCRIBER_CASSANDRA_DB_IP_ADDRESS"
        private const val PORT_ENV_NAME = "TRANSCRIBER_CASSANDRA_DB_PORT"
    }

    private val ipAddress: String
        get() = System.getenv(IP_ADDRESS_ENV_NAME)
            ?: throw IllegalArgumentException("No value was set for '$IP_ADDRESS_ENV_NAME' env variable")

    private val port: Int
        get() = System.getenv(PORT_ENV_NAME)?.toInt()
            ?: throw IllegalArgumentException("No value was set for '$PORT_ENV_NAME' env variable")

    val clientOptions: CassandraClientOptions
        get() = CassandraClientOptions()
            .addContactPoint(ipAddress)
            .setPort(port)
}

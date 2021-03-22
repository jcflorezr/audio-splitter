package net.jcflorezr.transcriber.core.config.db.cassandra

import io.vertx.cassandra.CassandraClient
import io.vertx.cassandra.CassandraClientOptions
import io.vertx.core.Vertx

abstract class CassandraConfig(private val properties: CassandraConfigProperties) {

    private val cassandraClient: CassandraClient
        get() = properties.run {
            CassandraClientOptions()
                .addContactPoint(ipAddress)
                .setPort(port)
                .setKeyspace(keyspaceName)
                .let { options -> CassandraClient.create(Vertx.vertx(), options) }
            }

        fun cassandraClient(): CassandraClient = cassandraClient
}

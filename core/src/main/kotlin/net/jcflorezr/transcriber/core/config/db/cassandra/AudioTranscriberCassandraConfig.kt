package net.jcflorezr.transcriber.core.config.db.cassandra

import io.vertx.cassandra.CassandraClient
import io.vertx.core.Vertx

object AudioTranscriberCassandraConfig : CassandraConfig {

    private const val KEYSPACE_NAME = "AUDIO_TRANSCRIBER"

    private val cassandraClient = super.clientOptions.setKeyspace(KEYSPACE_NAME)
        .let { options -> CassandraClient.create(Vertx.vertx(), options) }

    fun cassandraClient(): CassandraClient = cassandraClient
}

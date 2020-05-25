package net.jcflorezr.transcriber.core.config.db.cassandra

import io.vertx.cassandra.CassandraClient
import io.vertx.core.Vertx

object AudioSplitterCassandraConfig : CassandraConfig {

    private const val KEYSPACE_NAME = "AUDIO_SPLITTER"

    private val cassandraClient = super.clientOptions.setKeyspace(KEYSPACE_NAME)
        .let { options -> CassandraClient.create(Vertx.vertx(), options) }

    fun cassandraClient(): CassandraClient = cassandraClient
}

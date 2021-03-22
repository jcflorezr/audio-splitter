package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.sourcefileinfo

import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterIntegrationCassandraEnvProperties
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterIntegrationTestCassandraStartup

object SourceFileInfoCassandraDaoIntTestDI : CoroutineVerticle() {

    init {
        SourceFileInfoTablesCreation.createTablesInDb(AudioSplitterIntegrationTestCassandraStartup)
    }

    private val cassandraConfig =
        AudioSplitterCassandraConfig(AudioSplitterIntegrationCassandraEnvProperties.extract())
    private val sourceFileInfoCassandraDaoDI = SourceFileInfoCassandraDaoDI(cassandraConfig)
    val sourceFileInfoCassandraDao = sourceFileInfoCassandraDaoDI.sourceFileInfoCassandraDao

    override suspend fun start() {
        vertx.deployVerticleAwait(sourceFileInfoCassandraDao)
    }
}

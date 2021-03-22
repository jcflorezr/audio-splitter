package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audioclips

import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterIntegrationCassandraEnvProperties
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterIntegrationTestCassandraStartup

object AudioClipsCassandraDaoIntTestDI : CoroutineVerticle() {

    init {
        AudioClipsTablesCreation.createTablesInDb(AudioSplitterIntegrationTestCassandraStartup)
    }

    private val cassandraConfig =
        AudioSplitterCassandraConfig(AudioSplitterIntegrationCassandraEnvProperties.extract())
    private val audioClipsCassandraDaoDI = AudioClipsCassandraDaoDI(cassandraConfig)
    val audioClipsCassandraDao = audioClipsCassandraDaoDI.audioClipsCassandraDao

    override suspend fun start() {
        vertx.deployVerticleAwait(audioClipsCassandraDao)
    }
}

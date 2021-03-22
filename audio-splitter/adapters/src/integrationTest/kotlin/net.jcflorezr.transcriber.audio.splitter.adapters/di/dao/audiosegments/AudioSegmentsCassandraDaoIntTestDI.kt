package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments

import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterIntegrationCassandraEnvProperties
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterIntegrationTestCassandraStartup

// This class cannot be a singleton since we are using it for
// multiple test classes that need their own test context
class AudioSegmentsCassandraDaoIntTestDI : CoroutineVerticle() {

    init {
        AudioSegmentsTablesCreation.createTablesInDb(AudioSplitterIntegrationTestCassandraStartup)
    }

    private val cassandraConfig =
        AudioSplitterCassandraConfig(AudioSplitterIntegrationCassandraEnvProperties.extract())
    private val audioSegmentsCassandraDaoDI = AudioSegmentsCassandraDaoDI(cassandraConfig)
    private val basicAudioSegmentsCassandraDaoDI = BasicAudioSegmentsCassandraDaoDI(cassandraConfig)

    val audioSegmentsCassandraDao = audioSegmentsCassandraDaoDI.audioSegmentsCassandraDao
    val basicAudioSegmentsCassandraDao = basicAudioSegmentsCassandraDaoDI.basicAudioSegmentsCassandraDao

    override suspend fun start() {
        vertx.deployVerticleAwait(audioSegmentsCassandraDao)
        vertx.deployVerticleAwait(basicAudioSegmentsCassandraDao)
    }
}

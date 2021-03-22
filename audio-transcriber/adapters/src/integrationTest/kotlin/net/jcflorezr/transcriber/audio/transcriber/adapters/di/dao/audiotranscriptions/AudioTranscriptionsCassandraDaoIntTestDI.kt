package net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.audiotranscriptions

import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.AudioTranscriptionsCassandraDao
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberCassandraConfig
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberIntegrationCassandraEnvProperties
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberIntegrationTestCassandraStartup

object AudioTranscriptionsCassandraDaoIntTestDI : CoroutineVerticle() {

    init {
        AudioTranscriptionsTablesCreation.createTablesInDb(AudioTranscriberIntegrationTestCassandraStartup)
    }

    private val cassandraConfig =
        AudioTranscriberCassandraConfig(AudioTranscriberIntegrationCassandraEnvProperties.extract())
    private val cassandraDaoDI = AudioTranscriptionsCassandraDaoDI(cassandraConfig)
    val audioTranscriptionsCassandraDao: AudioTranscriptionsCassandraDao = cassandraDaoDI.audioTranscriptionsCassandraDao

    override suspend fun start() {
        vertx.deployVerticleAwait(audioTranscriptionsCassandraDao)
    }
}

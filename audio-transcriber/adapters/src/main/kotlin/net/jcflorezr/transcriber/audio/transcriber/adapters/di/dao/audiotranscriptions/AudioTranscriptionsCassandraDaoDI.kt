package net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.audiotranscriptions

import io.vertx.core.Vertx
import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.AudioTranscriptionsCassandraDao
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioTranscriberCassandraConfig

class AudioTranscriptionsCassandraDaoDI(
    audioTranscriberCassandraConfig: AudioTranscriberCassandraConfig
) {

    val audioTranscriptionsCassandraDao =
        AudioTranscriptionsCassandraDao(audioTranscriberCassandraConfig.cassandraClient())

    init {
        Vertx.vertx().deployVerticle(audioTranscriptionsCassandraDao)
    }
}

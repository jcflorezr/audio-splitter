package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audioclips

import io.vertx.core.Vertx
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips.AudioClipsCassandraDao
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig

class AudioClipsCassandraDaoDI(
    audioSplitterCassandraConfig: AudioSplitterCassandraConfig
) {

    val audioClipsCassandraDao = AudioClipsCassandraDao(audioSplitterCassandraConfig.cassandraClient())

    init {
        Vertx.vertx().deployVerticle(audioClipsCassandraDao)
    }
}

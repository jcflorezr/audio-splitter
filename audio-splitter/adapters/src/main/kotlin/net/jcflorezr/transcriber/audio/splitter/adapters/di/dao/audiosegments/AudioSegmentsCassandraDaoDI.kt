package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments

import io.vertx.core.Vertx
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.AudioSegmentsCassandraDao
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig

class AudioSegmentsCassandraDaoDI(
    audioSplitterCassandraConfig: AudioSplitterCassandraConfig
) {

    val audioSegmentsCassandraDao = AudioSegmentsCassandraDao(audioSplitterCassandraConfig.cassandraClient())

    init {
        Vertx.vertx().deployVerticle(audioSegmentsCassandraDao)
    }
}

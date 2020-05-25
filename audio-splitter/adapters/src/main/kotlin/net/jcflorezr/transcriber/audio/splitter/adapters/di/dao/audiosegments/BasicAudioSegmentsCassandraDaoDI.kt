package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments

import io.vertx.core.Vertx
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.BasicAudioSegmentsCassandraDao
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig

class BasicAudioSegmentsCassandraDaoDI(
    audioSplitterCassandraConfig: AudioSplitterCassandraConfig
) {

    val basicAudioSegmentsCassandraDao = BasicAudioSegmentsCassandraDao(audioSplitterCassandraConfig.cassandraClient())

    init {
        Vertx.vertx().deployVerticle(basicAudioSegmentsCassandraDao)
    }
}

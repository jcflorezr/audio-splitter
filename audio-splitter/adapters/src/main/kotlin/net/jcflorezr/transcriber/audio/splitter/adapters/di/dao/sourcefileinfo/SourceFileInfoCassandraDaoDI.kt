package net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.sourcefileinfo

import io.vertx.core.Vertx
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo.SourceFileInfoCassandraDao
import net.jcflorezr.transcriber.core.config.db.cassandra.AudioSplitterCassandraConfig

class SourceFileInfoCassandraDaoDI(
    audioSplitterCassandraConfig: AudioSplitterCassandraConfig
) {

    val sourceFileInfoCassandraDao = SourceFileInfoCassandraDao(audioSplitterCassandraConfig.cassandraClient())

    init {
        Vertx.vertx().deployVerticle(sourceFileInfoCassandraDao)
    }
}

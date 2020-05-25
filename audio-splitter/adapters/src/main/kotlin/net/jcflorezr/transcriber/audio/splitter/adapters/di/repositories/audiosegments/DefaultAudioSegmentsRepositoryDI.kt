package net.jcflorezr.transcriber.audio.splitter.adapters.di.repositories.audiosegments

import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments.AudioSegmentsCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments.BasicAudioSegmentsCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.audiosegments.DefaultAudioSegmentsRepository

class DefaultAudioSegmentsRepositoryDI(
    audioSegmentsCassandraDaoDI: AudioSegmentsCassandraDaoDI,
    basicAudioSegmentsCassandraDaoDI: BasicAudioSegmentsCassandraDaoDI
) {

    val defaultAudioSegmentsRepository = DefaultAudioSegmentsRepository(
        audioSegmentsCassandraDaoDI.audioSegmentsCassandraDao,
        basicAudioSegmentsCassandraDaoDI.basicAudioSegmentsCassandraDao
    )
}

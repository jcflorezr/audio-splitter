package net.jcflorezr.transcriber.audio.splitter.adapters.repositories.audiosegments

import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.AudioSegmentsCassandraDao
import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments.BasicAudioSegmentsCassandraDao
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.audiosegments.AudioSegmentsRepository

class DefaultAudioSegmentsRepository(
    private val audioSegmentsCassandraDao: AudioSegmentsCassandraDao,
    private val basicAudioSegmentsCassandraDao: BasicAudioSegmentsCassandraDao
) : AudioSegmentsRepository {

    override suspend fun findSegmentsRange(sourceAudioFileName: String, segmentStartInSeconds: Float, segmentEndInSeconds: Float) =
        audioSegmentsCassandraDao.findRange(sourceAudioFileName, segmentStartInSeconds, segmentEndInSeconds)
            .map { it.translate() }
            .toList()

    // TODO: where should this method be used? (in the event handler?)
    override suspend fun findBasicSegmentsBy(sourceAudioFileName: String) =
        basicAudioSegmentsCassandraDao.findBy(sourceAudioFileName)
            .map { it.translate() }

    override suspend fun save(audioSegment: AudioSegment) {
        audioSegmentsCassandraDao.save(audioSegmentsCassandraDao.toRecord(audioSegment))
    }
}

package net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.audiosegments

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment

interface AudioSegmentsRepository {

    suspend fun findBy(sourceAudioFileName: String, segmentStartInSeconds: Float): AudioSegment

    suspend fun findSegmentsRange(sourceAudioFileName: String, segmentStartInSeconds: Float, segmentEndInSeconds: Float): List<AudioSegment>

    suspend fun findBasicSegmentsBy(sourceAudioFileName: String): List<BasicAudioSegment>

    suspend fun save(audioSegment: AudioSegment)
}

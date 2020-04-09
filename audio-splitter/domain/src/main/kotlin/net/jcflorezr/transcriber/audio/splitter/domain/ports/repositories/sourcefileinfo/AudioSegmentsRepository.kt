package net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment

interface AudioSegmentsRepository {

    suspend fun findBy(
            sourceAudioFileName: String, segmentStartInSeconds: Float, segmentEndInSeconds: Float): List<AudioSegment>
}
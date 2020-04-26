package net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment

interface AudioClipsInfoService {
    suspend fun generateActiveSegments(audioSegments: List<BasicAudioSegment>)
}

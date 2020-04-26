package net.jcflorezr.transcriber.audio.splitter.domain.events.audiosegments

import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioClipsInfoService
import net.jcflorezr.transcriber.core.domain.EventHandler

class AudioSegmentsGeneratedHandler(
    private val audioClipsInfoService: AudioClipsInfoService
) : EventHandler<AudioSegmentsGenerated> {

    override suspend fun execute(event: AudioSegmentsGenerated) {
        audioClipsInfoService.generateActiveSegments(event.audioSegments.basicAudioSegments)
    }
}

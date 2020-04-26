package net.jcflorezr.transcriber.audio.transcriber.domain.events.audioclips

import net.jcflorezr.transcriber.audio.transcriber.domain.ports.aggregates.application.audiotranscriptions.AudioTranscriptionsService
import net.jcflorezr.transcriber.core.domain.EventHandler
import net.jcflorezr.transcriber.core.domain.events.audioclips.AudioClipFileGenerated

class AudioClipFileGeneratedHandler(
    private val audioTranscriptionsService: AudioTranscriptionsService
) : EventHandler<AudioClipFileGenerated> {

    override suspend fun execute(event: AudioClipFileGenerated) {
        audioTranscriptionsService.transcribe(event.audioClipFileInfo)
    }
}

package net.jcflorezr.transcriber.audio.transcriber.domain.events.audioclips

import net.jcflorezr.transcriber.audio.transcriber.domain.ports.aggregates.application.audiotranscriptions.AudioTranscriptionService
import net.jcflorezr.transcriber.core.domain.EventHandler
import net.jcflorezr.transcriber.core.domain.events.audioclips.AudioClipFileGenerated

class AudioClipFileGeneratedHandler(
    private val audioTranscriptionService: AudioTranscriptionService
) : EventHandler<AudioClipFileGenerated> {

    override suspend fun execute(event: AudioClipFileGenerated) {
        audioTranscriptionService.transcribe(event.audioClipFileInfo)
    }
}
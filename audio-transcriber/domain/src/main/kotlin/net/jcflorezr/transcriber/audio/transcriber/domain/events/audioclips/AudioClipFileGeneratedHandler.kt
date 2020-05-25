package net.jcflorezr.transcriber.audio.transcriber.domain.events.audioclips

import net.jcflorezr.transcriber.audio.transcriber.domain.ports.aggregates.application.audiotranscriptions.AudioTranscriptionsService
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Event
import net.jcflorezr.transcriber.core.domain.EventHandler
import net.jcflorezr.transcriber.core.domain.EventRouter
import net.jcflorezr.transcriber.core.domain.events.audioclips.AudioClipFileGenerated

class AudioClipFileGeneratedHandler(
    private val audioTranscriptionsService: AudioTranscriptionsService
) : EventHandler<Event<AggregateRoot>> {

    init {
        EventRouter.register(AudioClipFileGenerated::class.java, this)
    }

    override suspend fun execute(event: Event<AggregateRoot>) {
        val audioClipFileGenerated = event as AudioClipFileGenerated
        audioTranscriptionsService.transcribe(audioClipFileGenerated.audioClipFileInfo)
    }
}

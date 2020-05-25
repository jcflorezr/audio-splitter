package net.jcflorezr.transcriber.audio.transcriber.domain.commands

import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.events.audiotranscriptions.AudioTranscriptionGenerated
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.repositories.audiotranscriptions.AudioTranscriptionsRepository
import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.domain.EventDispatcher

class GenerateAudioTranscription(
    private val audioTranscriptionsRepository: AudioTranscriptionsRepository,
    private val commandDispatcher: EventDispatcher
) : Command<AudioTranscription> {

    override suspend fun execute(aggregateRoot: AudioTranscription) {
        audioTranscriptionsRepository.save(aggregateRoot)
        commandDispatcher.publish(AudioTranscriptionGenerated(aggregateRoot))
    }
}

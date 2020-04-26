package net.jcflorezr.transcriber.audio.transcriber.domain.commands

import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.repositories.audiotranscriptions.AudioTranscriptionsRepository
import net.jcflorezr.transcriber.core.domain.Command

class GenerateAudioTranscription(
    private val audioTranscriptionsRepository: AudioTranscriptionsRepository
) : Command<AudioTranscription> {

    override suspend fun execute(aggregateRoot: AudioTranscription) {
        audioTranscriptionsRepository.save(aggregateRoot)
        aggregateRoot.run { audioTranscriptionsRepository.findBy(sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond) }
    }
}
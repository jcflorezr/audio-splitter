package net.jcflorezr.transcriber.audio.transcriber.domain.commands

import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.core.domain.Command

class GenerateAudioTranscription : Command<AudioTranscription> {

    override suspend fun execute(aggregateRoot: AudioTranscription) {

    }
}
package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.GeneratedAudioClip
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient
import net.jcflorezr.transcriber.core.domain.Command

interface AudioTranscriptionService {
    suspend fun transcribe(generatedAudioClip: GeneratedAudioClip)
}

class AudioTranscriptionServiceImpl(
    private val audioTranscriptionsClient: AudioTranscriptionsClient,
    private val command: Command
) : AudioTranscriptionService {

    override suspend fun transcribe(generatedAudioClip: GeneratedAudioClip) = coroutineScope<Unit> {
        val transcriptionAlternatives =
            audioTranscriptionsClient.getAudioTranscriptionAlternatives(generatedAudioClip.audioClipFile.absolutePath)
        val audioTranscription = AudioTranscription.createNew(generatedAudioClip, transcriptionAlternatives)
        launch { command.execute(aggregateRoot = audioTranscription) }
    }
}
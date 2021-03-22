package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions

import java.io.File
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.aggregates.application.audiotranscriptions.AudioTranscriptionsService
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient
import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo

class AudioTranscriptionsServiceImpl(
    private val audioTranscriptionsClient: AudioTranscriptionsClient,
    private val clipsFilesDirectory: String,
    private val command: Command<AudioTranscription>
) : AudioTranscriptionsService {

    override suspend fun transcribe(audioClipFileInfo: AudioClipFileInfo) = coroutineScope<Unit> {
        val audioClipFile = audioClipFileInfo.run {
            File("$clipsFilesDirectory/$audioClipFileName.$audioClipFileExtension")
        }
        val transcriptionAlternatives =
            audioTranscriptionsClient.getAudioTranscriptionAlternatives(audioClipFile.absolutePath)
        val audioTranscription = AudioTranscription.createNew(audioClipFileInfo, transcriptionAlternatives)
        launch { command.execute(aggregateRoot = audioTranscription) }
    }
}

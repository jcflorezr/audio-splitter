package net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.adapters.AudioFileMetadataGenerator
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.adapters.AudioWavConverter
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.application.AudioSourceFileInfoService
import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.domain.ports.storage.StorageClient

class AudioSourceFileInfoServiceImpl(
    private val storageClient: StorageClient,
    private val audioWavConverter: AudioWavConverter,
    private val audioFileMetadataGenerator: AudioFileMetadataGenerator,
    private val command: Command<AudioSourceFileInfo>
) : AudioSourceFileInfoService {

    override suspend fun extractAudioInfoFromSourceFile(audioFileName: String) = coroutineScope<Unit> {
        val audioFile = storageClient.retrieveFileFromStorage(audioFileName)
        val wavFile = withContext(Dispatchers.Default) { audioWavConverter.createAudioWavFile(audioFile) }
        val audioFileContentInfo = AudioContentInfo.extractFrom(audioFile = wavFile ?: audioFile)
        val audioMetadata = async { audioFileMetadataGenerator.retrieveAudioFileMetadata(audioFile) }
        val audioSourceFileInfo = AudioSourceFileInfo(
            originalAudioFile = audioFile.name,
            audioContentInfo = audioFileContentInfo,
            convertedAudioFile = wavFile?.name,
            metadata = audioMetadata.await()
        )
        launch { command.execute(audioSourceFileInfo) }
    }
}

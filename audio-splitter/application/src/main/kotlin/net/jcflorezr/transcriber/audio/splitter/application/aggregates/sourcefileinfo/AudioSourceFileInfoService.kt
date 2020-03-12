package net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.domain.ports.cloud.storage.CloudStorageClient
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.AudioFileMetadataGenerator
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.AudioWavConverter
import net.jcflorezr.transcriber.core.domain.Command

interface AudioSourceFileInfoService {
    suspend fun extractAudioInfoFromSourceFile(audioFileName: String)
}

class AudioSourceFileInfoServiceImpl(
    private val cloudStorageClient: CloudStorageClient,
    private val audioWavConverter: AudioWavConverter,
    private val audioFileMetadataGenerator: AudioFileMetadataGenerator,
    private val command: Command
) : AudioSourceFileInfoService {

    override suspend fun extractAudioInfoFromSourceFile(audioFileName: String) = coroutineScope<Unit> {
        val audioFile = cloudStorageClient.downloadFileFromStorage(audioFileName)
        val wavFile = withContext(Dispatchers.Default) { audioWavConverter.createAudioWavFile(audioFile) }
        val audioFileContentInfo = AudioContentInfo.create(audioFile = wavFile ?: audioFile)
        val audioMetadata = async { audioFileMetadataGenerator.retrieveAudioFileMetadata(audioFile) }
        val audioSourceFileInfo = AudioSourceFileInfo(
            originalAudioFile = audioFile.name,
            audioContentInfo = audioFileContentInfo,
            convertedAudioFile = wavFile?.name,
            metadata = audioMetadata.await())
        launch { command.execute(audioSourceFileInfo) }
    }
}
package net.jcflorezr.audio.splitter.application.sourcefile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import net.jcflorezr.audio.splitter.domain.cloud.storage.CloudStorageClient
import net.jcflorezr.audio.splitter.domain.sourcefile.AudioContentInfo
import net.jcflorezr.audio.splitter.domain.sourcefile.AudioFileMetadataGenerator
import net.jcflorezr.audio.splitter.domain.sourcefile.AudioSourceFileInfo
import net.jcflorezr.audio.splitter.domain.sourcefile.AudioWavConverter

interface AudioSourceFileInfoService {
    suspend fun extractAudioInfoFromSourceFile(audioFileName: String): AudioSourceFileInfo
}

class AudioSourceFileInfoServiceImpl(
    private val cloudStorageClient: CloudStorageClient,
    private val audioWavConverter: AudioWavConverter,
    private val audioFileMetadataGenerator: AudioFileMetadataGenerator
) : AudioSourceFileInfoService {

    override suspend fun extractAudioInfoFromSourceFile(audioFileName: String): AudioSourceFileInfo = coroutineScope {
        val audioFile = cloudStorageClient.downloadFileFromStorage(audioFileName)
        val wavFile = withContext(Dispatchers.Default) { audioWavConverter.createAudioWavFile(audioFile) }
        val audioFileContentInfo = AudioContentInfo.create(audioFile = wavFile ?: audioFile)
        val audioMetadata = async { audioFileMetadataGenerator.retrieveAudioFileMetadata(audioFile) }
        AudioSourceFileInfo(
            originalAudioFile = audioFile.name,
            audioContentInfo = audioFileContentInfo,
            convertedAudioFile = wavFile?.name,
            metadata = audioMetadata.await())
    }
}
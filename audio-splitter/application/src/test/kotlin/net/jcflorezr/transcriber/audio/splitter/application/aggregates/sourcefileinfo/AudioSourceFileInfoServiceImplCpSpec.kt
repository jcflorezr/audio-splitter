package net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.application.di.AudioSourceFileInfoServiceImplCpSpecDI
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.application.AudioSourceFileInfoService
import net.jcflorezr.transcriber.audio.splitter.domain.ports.cloud.storage.CloudStorageClient
import net.jcflorezr.transcriber.core.exception.AudioSourceException
import net.jcflorezr.transcriber.core.exception.CloudStorageFileException
import net.jcflorezr.transcriber.core.exception.FileException
import net.jcflorezr.transcriber.core.util.SupportedAudioFormats
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when` as When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AudioSourceFileInfoServiceImplCpSpecDI::class])
internal class AudioSourceFileInfoServiceImplCpSpec {

    @Autowired
    private lateinit var audioSourceFileInfoServiceImpl: AudioSourceFileInfoService
    @Autowired
    private lateinit var cloudStorageClient: CloudStorageClient

    private val thisClass: Class<AudioSourceFileInfoServiceImplCpSpec> = this.javaClass
    private val sourceFilesPath: String
    private val tempSourceFilesPath: String

    init {
        sourceFilesPath = thisClass.getResource("/source-file-info").path
        tempSourceFilesPath = thisClass.getResource("/temp-converted-files/source-file-info").path
    }

    @Test
    fun extractAudioInfoFromMp3File() = runBlocking {
        extractAudioInfoFromSourceFile(audioFileExtension = SupportedAudioFormats.MP3.extension)
    }

    @Test
    fun extractAudioInfoFromFlacFile() = runBlocking {
        extractAudioInfoFromSourceFile(audioFileExtension = SupportedAudioFormats.FLAC.extension)
    }

    @Test
    fun extractAudioInfoFromWavFile() = runBlocking {
        extractAudioInfoFromSourceFile(audioFileExtension = SupportedAudioFormats.WAV.extension)
    }

    @Test
    fun downloadedAudioFileWasNotFound() = runBlocking {
        val audioFileName = "any-audio-file-name"
        When(cloudStorageClient.retrieveFileFromStorage(audioFileName))
            .thenThrow(FileException.fileNotFound(audioFileName))
        val actualException = Assertions.assertThrows(FileException::class.java) {
            runBlocking {
                audioSourceFileInfoServiceImpl.extractAudioInfoFromSourceFile(audioFileName)
            }
        }
        val expectedException = FileException.fileNotFound(audioFileName)
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
    }

    @Test
    fun downloadedAudioFileIsTooShort() = runBlocking(Dispatchers.IO) {
        // Given
        val audioFileName = "too-short-test-audio-mono.wav"
        val sourceFile = File("$sourceFilesPath/$audioFileName")
        val tempFile = File("$tempSourceFilesPath/$audioFileName")
        FileUtils.copyFile(sourceFile, tempFile)

        try {
            // When
            When(cloudStorageClient.retrieveFileFromStorage(audioFileName)).thenReturn(tempFile)
            val actualException = Assertions.assertThrows(AudioSourceException::class.java) {
                runBlocking {
                    audioSourceFileInfoServiceImpl.extractAudioInfoFromSourceFile(audioFileName)
                }
            }

            // Then
            val expectedException = AudioSourceException.audioSourceTooShort()
            assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
            assertThat(actualException.message, Is(equalTo(expectedException.message)))
        } finally {
            FileUtils.cleanDirectory(File(tempSourceFilesPath))
        }
    }

    @Test
    fun audioFileWasNotFoundInCloudStorage() = runBlocking {
        val audioFileName = "any-audio-file-name-2"
        When(cloudStorageClient.retrieveFileFromStorage(audioFileName))
            .thenThrow(CloudStorageFileException.fileNotFoundInCloudStorage(audioFileName))
        val actualException = Assertions.assertThrows(CloudStorageFileException::class.java) {
            runBlocking {
                audioSourceFileInfoServiceImpl.extractAudioInfoFromSourceFile(audioFileName)
            }
        }
        val expectedException = CloudStorageFileException.fileNotFoundInCloudStorage(audioFileName)
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
    }

    private suspend fun extractAudioInfoFromSourceFile(audioFileExtension: String) = withContext(Dispatchers.IO) {
        // Given
        val audioFileName = "test-audio-mono.$audioFileExtension"
        val sourceFile = File("$sourceFilesPath/$audioFileName")
        val tempFile = File("$tempSourceFilesPath/$audioFileName")
        FileUtils.copyFile(sourceFile, tempFile)

        try {
            // When
            When(cloudStorageClient.retrieveFileFromStorage(audioFileName)).thenReturn(tempFile)
            audioSourceFileInfoServiceImpl.extractAudioInfoFromSourceFile(audioFileName)
        } finally {
            FileUtils.cleanDirectory(File(tempSourceFilesPath))
        }
    }
}

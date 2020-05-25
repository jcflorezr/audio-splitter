package net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.sourcefileinfo.AudioSourceFileInfoServiceImplCpSpecDI
import net.jcflorezr.transcriber.core.exception.AudioSourceException
import net.jcflorezr.transcriber.core.exception.CloudStorageFileException
import net.jcflorezr.transcriber.core.exception.FileException
import net.jcflorezr.transcriber.core.util.SupportedAudioFormats
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when` as When

@ObsoleteCoroutinesApi
@ExtendWith(VertxExtension::class)
internal class AudioSourceFileInfoServiceImplCpSpec {

    private val sourceFilesPath = this.javaClass.getResource("/source-file-info").path
    private val tempSourceFilesPath = this.javaClass.getResource("/temp-converted-files/source-file-info").path

    private val audioSourceFileInfoServiceImplCpSpecDI = AudioSourceFileInfoServiceImplCpSpecDI
    private val eventHandler = audioSourceFileInfoServiceImplCpSpecDI.sourceFileInfoGeneratedEventHandler()
    private val audioSourceFileInfoServiceImpl = audioSourceFileInfoServiceImplCpSpecDI.audioSourceFileInfoServiceTest
    private val cloudStorageClient = audioSourceFileInfoServiceImplCpSpecDI.googleCloudStorageClientTest

    @BeforeAll
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(audioSourceFileInfoServiceImplCpSpecDI, testContext.completing())
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
        testContext.completeNow()
    }

    @Nested
    inner class SuccessScenarios {

        @Test
        fun `extract audio info from mp3 source file`(testContext: VertxTestContext) = runBlocking {
            extractAudioInfoFromSourceFile(SupportedAudioFormats.MP3.extension, testContext)
        }

        @Test
        fun `extract audio info from flac source file`(testContext: VertxTestContext) = runBlocking {
            extractAudioInfoFromSourceFile(SupportedAudioFormats.FLAC.extension, testContext)
        }

        @Test
        fun `extract audio info from wav source file`(testContext: VertxTestContext) = runBlocking {
            extractAudioInfoFromSourceFile(SupportedAudioFormats.WAV.extension, testContext)
        }

        private suspend fun extractAudioInfoFromSourceFile(
            audioFileExtension: String,
            testContext: VertxTestContext
        ) = withContext(Dispatchers.IO) {
            // Given
            val audioFileName = "test-audio-mono.$audioFileExtension"
            val sourceFile = File("$sourceFilesPath/$audioFileName")
            val tempFile = File("$tempSourceFilesPath/$audioFileName")
            FileUtils.copyFile(sourceFile, tempFile)

            try {
                // When
                When(cloudStorageClient.retrieveFileFromStorage(audioFileName)).thenReturn(tempFile)
                audioSourceFileInfoServiceImpl.extractAudioInfoFromSourceFile(audioFileName)
                eventHandler.assertSourceFileInfo(audioFileName, testContext)
            } finally {
                tempFile.delete()
            }
        }
    }

    @Nested
    inner class FailedScenarios {

        @Test
        fun `downloaded audio file was not found`() = runBlocking {
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
        fun `audio file was not found in cloud storage`() = runBlocking {
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

        @Test
        fun `downloaded audio file is too short`() = runBlocking(Dispatchers.IO) {
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
                tempFile.delete()
            }
        }
    }
}

package net.jcflorezr.transcriber.audio.splitter.adapters.cloud.storage

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import java.io.File
import java.nio.file.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.jcflorezr.transcriber.audio.splitter.adapters.di.cloud.storage.GoogleCloudStorageClientTestDI
import net.jcflorezr.transcriber.core.exception.CloudStorageFileException
import net.jcflorezr.transcriber.core.exception.FileException
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when` as When
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock

internal class GoogleCloudStorageClientTest {

    private val googleCloudStorageClient = GoogleCloudStorageClientTestDI.googleCloudStorageClientTest
    private val storage: Storage = GoogleCloudStorageClientTestDI.storageClient
    private val bucketDirectory = GoogleCloudStorageClientTestDI.bucketDirectory
    private val bucketName = GoogleCloudStorageClientTestDI.bucketName

    private val thisClass: Class<GoogleCloudStorageClientTest> = this.javaClass
    private val sourceFilePath: String = thisClass.getResource("/source-file-info").path
    private val tempConvertedFilesPath: String = thisClass.getResource("/temp-converted-files").path
    private val audioFileName: String = "test-audio-mono.wav"

    @Test
    fun downloadFileFromStorage() = runBlocking(Dispatchers.IO) {
        val sourceFile = File("$sourceFilePath/$audioFileName")
        val tempFile = File("$tempConvertedFilesPath/$audioFileName")
        try {
            val blobMock = mock(Blob::class.java)
            FileUtils.copyFile(sourceFile, tempFile)
            When(storage.get(BlobId.of(bucketName, "$bucketDirectory/$audioFileName"))).thenReturn(blobMock)
            doNothing().`when`(blobMock).downloadTo(Paths.get(tempFile.absolutePath))
            val downloadedFileFromStorage = googleCloudStorageClient.retrieveFileFromStorage(fileName = audioFileName)
            assertTrue(downloadedFileFromStorage.exists())
            assertThat(downloadedFileFromStorage.absolutePath, Is(equalTo(tempFile.absolutePath)))
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun audioFileWasNotFoundInCloudStorage() {
        When(storage.get(BlobId.of(bucketName, "$bucketDirectory/$audioFileName"))).thenReturn(null)
        val actualException = assertThrows(CloudStorageFileException::class.java) {
            runBlocking {
                googleCloudStorageClient.retrieveFileFromStorage(fileName = audioFileName)
            }
        }
        val expectedException = CloudStorageFileException.fileNotFoundInCloudStorage(audioFileName)
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
    }

    @Test
    fun downloadedAudioFileWasNotFound() {
        val tempFile = File("$tempConvertedFilesPath/$audioFileName")
        val blobMock = mock(Blob::class.java)
        When(storage.get(BlobId.of(bucketName, "$bucketDirectory/$audioFileName"))).thenReturn(blobMock)
        doNothing().`when`(blobMock).downloadTo(Paths.get(tempFile.absolutePath))
        val actualException = assertThrows(FileException::class.java) {
            runBlocking {
                googleCloudStorageClient.retrieveFileFromStorage(fileName = audioFileName)
            }
        }
        val expectedException = FileException.fileNotFound(tempFile.absolutePath)
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
    }
}

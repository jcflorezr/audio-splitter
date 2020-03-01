package net.jcflorezr.audio.splitter.adapters.cloud.storage

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.jcflorezr.audio.splitter.adapters.di.GoogleCloudStorageClientTestDI
import net.jcflorezr.audio.splitter.domain.exception.CloudStorageFileException
import net.jcflorezr.audio.splitter.domain.exception.TempLocalFileException
import org.apache.commons.io.FileUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.file.Paths

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [GoogleCloudStorageClientTestDI::class])
internal class GoogleCloudStorageClientTest {

    @Autowired
    private lateinit var googleCloudStorageClient: GoogleCloudStorageClient
    @Autowired
    private lateinit var storage: Storage
    @Autowired
    @Qualifier("bucketDirectory")
    private lateinit var bucketDirectory: String
    @Autowired
    @Qualifier("bucketName")
    private lateinit var bucketName: String

    private val thisClass: Class<GoogleCloudStorageClientTest> = this.javaClass
    private val sourceFilePath: String
    private val tempConvertedFilesPath: String
    private val audioFileName: String

    init {
        sourceFilePath = thisClass.getResource("/source-file").path
        tempConvertedFilesPath = thisClass.getResource("/temp-converted-files").path
        audioFileName = "test-audio-mono.wav"
    }

    @Test
    fun downloadFileFromStorage() = runBlocking(Dispatchers.IO) {
        val sourceFile = File("$sourceFilePath/$audioFileName")
        val tempFile = File("$tempConvertedFilesPath/$audioFileName")
        try {
            val blobMock = mock(Blob::class.java)
            FileUtils.copyFile(sourceFile, tempFile)
            When(storage.get(BlobId.of(bucketName, "$bucketDirectory/$audioFileName"))).thenReturn(blobMock)
            doNothing().`when`(blobMock).downloadTo(Paths.get(tempFile.absolutePath))
            val downloadedFileFromStorage = googleCloudStorageClient.downloadFileFromStorage(fileName = audioFileName)
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
                googleCloudStorageClient.downloadFileFromStorage(fileName = audioFileName)
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
        val actualException = assertThrows(TempLocalFileException::class.java) {
            runBlocking {
                googleCloudStorageClient.downloadFileFromStorage(fileName = audioFileName)
            }
        }
        val expectedException = TempLocalFileException.tempDownloadedFileNotFound(tempFile.absolutePath)
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
    }
}
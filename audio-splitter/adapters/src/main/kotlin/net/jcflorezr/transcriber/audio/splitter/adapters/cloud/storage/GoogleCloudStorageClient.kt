package net.jcflorezr.transcriber.audio.splitter.adapters.cloud.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import java.io.File
import java.nio.file.Paths
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.jcflorezr.transcriber.audio.splitter.domain.ports.cloud.storage.CloudStorageClient
import net.jcflorezr.transcriber.core.exception.CloudStorageFileException
import net.jcflorezr.transcriber.core.exception.FileException
import org.apache.tika.Tika

class GoogleCloudStorageClient(
    private val bucketName: String,
    private val bucketDirectory: String,
    private val tempLocalDirectory: String,
    private val storageClient: Storage
) : CloudStorageClient {

    private val logger = KotlinLogging.logger { }

    override suspend fun retrieveFileFromStorage(fileName: String) = withContext(Dispatchers.IO) {
        logger.info { "[1][entry-point] Downloading source audio file: ($fileName) from bucket" }
        val blobId = BlobId.of(bucketName, "$bucketDirectory/$fileName")
        val blob = storageClient.get(blobId)
            ?: throw CloudStorageFileException.fileNotFoundInCloudStorage(fileName)
        val pathToDownloadFile = "$tempLocalDirectory/$fileName"
        blob.downloadTo(Paths.get(pathToDownloadFile))
        val downloadedFile = File(pathToDownloadFile)
        when {
            !downloadedFile.exists() -> throw FileException.fileNotFound(downloadedFile.absolutePath)
            else -> downloadedFile
        }
    }

    override suspend fun sendFileToStorage(file: File, transactionId: String) = withContext<Unit>(Dispatchers.IO) {
        val audioFileName = file.name
        logger.info { "[$transactionId][6][audio-clip] Uploading Audio File ($audioFileName) to bucket." }
        val enclosingFolderPath = "${transactionId.substringAfter("_")}/$transactionId"
        val audioContentType = Tika().detect(file)
        val blobIdForFile = BlobId.of(bucketName, "$bucketDirectory/$enclosingFolderPath/$audioFileName")
        val blobInfoForFile = BlobInfo.newBuilder(blobIdForFile).setContentType(audioContentType).build()
        storageClient.create(blobInfoForFile, file.readBytes())
        file.delete()
    }
}

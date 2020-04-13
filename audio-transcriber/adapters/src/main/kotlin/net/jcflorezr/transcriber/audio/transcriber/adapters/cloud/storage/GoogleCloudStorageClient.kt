package net.jcflorezr.transcriber.audio.transcriber.adapters.cloud.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.storage.CloudStorageClient
import net.jcflorezr.transcriber.core.exception.CloudStorageFileException
import net.jcflorezr.transcriber.core.exception.FileException
import java.io.File
import java.nio.file.Paths

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
}
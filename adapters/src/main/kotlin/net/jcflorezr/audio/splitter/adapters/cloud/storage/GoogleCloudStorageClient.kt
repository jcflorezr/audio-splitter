package net.jcflorezr.audio.splitter.adapters.cloud.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.jcflorezr.audio.splitter.domain.cloud.storage.CloudStorageClient
import net.jcflorezr.audio.splitter.domain.exception.CloudStorageFileException
import net.jcflorezr.audio.splitter.domain.exception.InternalServerErrorException
import net.jcflorezr.audio.splitter.domain.exception.TempLocalFileException
import org.apache.tika.Tika
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths

class GoogleCloudStorageClient(
    private val bucketName: String,
    private val bucketDirectory: String,
    private val tempLocalDirectory: String,
    private val storageClient: Storage
) : CloudStorageClient {

    private val logger = KotlinLogging.logger { }

    override suspend fun downloadFileFromStorage(fileName: String) = withContext(Dispatchers.IO) {
        logger.info { "[1][entry-point] Downloading source audio file: ($fileName) from bucket" }
        val blobId = BlobId.of(bucketName, "$bucketDirectory/$fileName")
        val blob = storageClient.get(blobId)
            ?: throw CloudStorageFileException.fileNotFoundInCloudStorage(fileName)
        val pathToDownloadFile = "$tempLocalDirectory/$fileName"
        blob.downloadTo(Paths.get(pathToDownloadFile))
        val downloadedFile = File(pathToDownloadFile)
        when {
            !downloadedFile.exists() -> throw TempLocalFileException.tempDownloadedFileNotFound(downloadedFile.absolutePath)
            else -> downloadedFile
        }
    }

    override suspend fun uploadFileToStorage(file: File, transactionId: String) = withContext<Unit>(Dispatchers.IO) {
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
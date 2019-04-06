package net.jcflorezr.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import mu.KotlinLogging
import net.jcflorezr.exception.SourceAudioFileValidationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Paths
import javax.annotation.PostConstruct
import com.google.cloud.storage.BlobInfo
import net.jcflorezr.util.AudioUtils

interface BucketClient {
    fun downloadSourceFileFromBucket(audioFileName: String): File
    fun uploadFileToBucket(audioFile: File, transactionId: String)
}

@Service
final class BucketClientImpl : BucketClient {

    @Value("\${files-config.bucket-name}")
    private lateinit var bucketName: String
    @Value("\${files-config.bucket-directory}")
    private lateinit var bucketDirectory: String

    private val thisClass: Class<BucketClientImpl> = this.javaClass
    private lateinit var tempDirectory: String
    private lateinit var bucketInstance: Storage

    private val logger = KotlinLogging.logger { }

    @PostConstruct
    fun init() {
        bucketInstance = StorageOptions.getDefaultInstance().service
        tempDirectory = thisClass.getResource("/temp-converted-files").path
    }

    override fun downloadSourceFileFromBucket(audioFileName: String): File {
        logger.info { "[1][entry-point] Downloading source audio file: ($audioFileName) from bucket" }
        val blobId = BlobId.of(bucketName, "$bucketDirectory/$audioFileName")
        val blob = bucketInstance.get(blobId)
            ?: throw SourceAudioFileValidationException.audioFileDoesNotExistInBucket(audioFileName)
        val downloadedFilePath = "$tempDirectory/$audioFileName"
        blob.downloadTo(Paths.get(downloadedFilePath))
        return File(downloadedFilePath)
    }

    override fun uploadFileToBucket(audioFile: File, transactionId: String) {
        val audioFileName = audioFile.name
        logger.info { "[$transactionId][6][audio-clip] Uploading Audio File ($audioFileName) to bucket." }
        val enclosingFolderPath = "${transactionId.substringAfter("_")}/$transactionId"
        // upload audio file
        val audioContentType = AudioUtils.tikaAudioParser.detect(audioFile)
        val blobIdForFile = BlobId.of(bucketName, "$bucketDirectory/$enclosingFolderPath/$audioFileName")
        val blobInfoForFile = BlobInfo.newBuilder(blobIdForFile).setContentType(audioContentType).build()
        bucketInstance.create(blobInfoForFile, audioFile.readBytes())
        audioFile.delete()
    }
}

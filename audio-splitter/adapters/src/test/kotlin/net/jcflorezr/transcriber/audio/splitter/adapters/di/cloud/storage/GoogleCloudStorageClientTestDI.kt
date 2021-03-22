package net.jcflorezr.transcriber.audio.splitter.adapters.di.cloud.storage

import com.google.cloud.storage.Storage
import net.jcflorezr.transcriber.audio.splitter.adapters.cloud.storage.GoogleCloudStorageClient
import org.mockito.Mockito.mock

object GoogleCloudStorageClientTestDI {

    private val thisClass: Class<GoogleCloudStorageClientTestDI> = this.javaClass
    val storageClient: Storage = mock(Storage::class.java)
    const val bucketName = "any-bucket-name"
    const val bucketDirectory = "any-bucket-directory"

    val googleCloudStorageClientTest = GoogleCloudStorageClient(bucketName, bucketDirectory, tempLocalDirectory(), storageClient)

    private fun tempLocalDirectory() = thisClass.getResource("/temp-converted-files").path
}

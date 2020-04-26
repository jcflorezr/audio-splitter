package net.jcflorezr.transcriber.audio.splitter.adapters.di.cloud.storage

import com.google.cloud.storage.Storage
import net.jcflorezr.transcriber.audio.splitter.adapters.cloud.storage.GoogleCloudStorageClient
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class GoogleCloudStorageClientTestDI {

    private val thisClass: Class<GoogleCloudStorageClientTestDI> = this.javaClass

    @Bean open fun googleCloudStorageClientTest() =
        GoogleCloudStorageClient(
            storageClient = storageClient(),
            bucketName = bucketName(),
            bucketDirectory = bucketDirectory(),
            tempLocalDirectory = tempLocalDirectory())

    @Bean open fun storageClient(): Storage = mock(Storage::class.java)

    @Bean open fun bucketName() = "any-bucket-name"

    @Bean open fun bucketDirectory() = "any-bucket-directory"

    private fun tempLocalDirectory() = thisClass.getResource("/temp-converted-files").path
}

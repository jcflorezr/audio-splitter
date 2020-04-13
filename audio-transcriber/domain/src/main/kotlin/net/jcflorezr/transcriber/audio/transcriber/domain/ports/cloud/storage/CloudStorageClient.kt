package net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.storage

import net.jcflorezr.transcriber.core.domain.ports.storage.StorageClient
import java.io.File
import java.lang.UnsupportedOperationException

interface CloudStorageClient : StorageClient {
    override suspend fun sendFileToStorage(file: File, transactionId: String) {
        throw UnsupportedOperationException("Not yet implemented")
    }
}
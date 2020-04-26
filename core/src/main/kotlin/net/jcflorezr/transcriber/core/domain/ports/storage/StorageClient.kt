package net.jcflorezr.transcriber.core.domain.ports.storage

import java.io.File

interface StorageClient {
    suspend fun retrieveFileFromStorage(fileName: String): File
    suspend fun sendFileToStorage(file: File, transactionId: String)
}

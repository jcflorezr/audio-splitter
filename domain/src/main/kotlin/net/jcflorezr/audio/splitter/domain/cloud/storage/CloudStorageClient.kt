package net.jcflorezr.audio.splitter.domain.cloud.storage

import java.io.File

interface CloudStorageClient {
    suspend fun downloadFileFromStorage(fileName: String): File
    suspend fun uploadFileToStorage(file: File, transactionId: String)
}
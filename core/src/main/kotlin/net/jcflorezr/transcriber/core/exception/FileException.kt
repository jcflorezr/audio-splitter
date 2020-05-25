package net.jcflorezr.transcriber.core.exception

import java.io.FileNotFoundException

class CloudStorageFileException(
    message: String,
    errorCode: String,
    suggestion: String? = null
) : BadRequestException(message = message, errorCode = errorCode, suggestion = suggestion) {
    companion object {

        fun fileNotFoundInCloudStorage(filePath: String) =
            CloudStorageFileException(
                errorCode = "file_not_found_in_cloud_storage",
                message = "File '$filePath' was not found in the implemented cloud storage.")
    }
}

class FileException(
    errorCode: String,
    exception: Exception
) : InternalServerErrorException(errorCode = errorCode, throwable = exception) {
    companion object {

        fun fileNotFound(filePath: String) =
            FileException(
                errorCode = "file_not_found_in_current_storage",
                exception = FileNotFoundException(
                    "The file placed in the current storage (cloud storage or local storage) was not found. File path: $filePath"))
    }
}

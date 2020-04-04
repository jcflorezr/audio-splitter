package net.jcflorezr.transcriber.audio.splitter.domain.exception

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

class TempLocalFileException(
    errorCode: String,
    exception: Exception
) : InternalServerErrorException(errorCode = errorCode, exception = exception) {
    companion object {

        fun tempDownloadedFileNotFound(filePath: String) =
            TempLocalFileException(
                errorCode = "downloaded_file_not_found",
                exception =
                FileNotFoundException("The downloaded file from bucket was not found in the local temp directory. File path: $filePath"))
    }
}
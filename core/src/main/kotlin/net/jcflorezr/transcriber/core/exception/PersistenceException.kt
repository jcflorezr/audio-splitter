package net.jcflorezr.transcriber.core.exception

class PersistenceException(
    message: String,
    errorCode: String
) : NotFoundException(message = message, errorCode = errorCode) {
    companion object {

        fun recordNotFoundInRepository(entityName: String, keys: Map<String, Any>) =
            PersistenceException(
                errorCode = "record_not_found",
                message = "Record was not found in '$entityName' searching by keys = $keys.")
    }
}

package net.jcflorezr.transcriber.core.exception

class PersistenceException {
    companion object {

        fun recordNotFoundInRepository(entityName: String, keys: Map<String, Any>) =
            NotFoundException(
                errorCode = "record_not_found_in_repository",
                message = "Record was not found in '$entityName' searching by keys = $keys.")

        fun recordNotSavedInRepository(throwable: Throwable) =
            InternalServerErrorException(
                errorCode = "record_not_saved_in_repository",
                throwable = throwable)
    }
}

package net.jcflorezr.entrypoint

import net.jcflorezr.exception.AudioSplitterException
import net.jcflorezr.exception.BadRequestException
import net.jcflorezr.exception.InternalServerErrorException
import net.jcflorezr.model.ErrorResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.context.request.WebRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApiErrorHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(BadRequestException::class)
    fun processBadRequestExceptions(e: BadRequestException, request: WebRequest): ResponseEntity<Any> {
        return processException(e, request, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InternalServerErrorException::class)
    fun processInternalServerErrorException(e: InternalServerErrorException, request: WebRequest): ResponseEntity<Any> {
        return processException(e, request, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(Exception::class)
    fun processGenericErrorException(e: Exception, request: WebRequest): ResponseEntity<Any> {
        return processException(InternalServerErrorException(ex = e), request, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    private fun processException(e: AudioSplitterException, request: WebRequest, httpStatus: HttpStatus): ResponseEntity<Any> {
        val errorResponse = ErrorResponse(e)
        return handleExceptionInternal(e, errorResponse, headersAsJsonTypeResponse(), httpStatus, request)
    }

    private fun headersAsJsonTypeResponse(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON_UTF8
        return headers
    }
}
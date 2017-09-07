package net.jcflorezr.api.endpoint;

import net.jcflorezr.exceptions.AudioSplitterCustomException;
import net.jcflorezr.exceptions.BadRequestException;
import net.jcflorezr.exceptions.InternalServerErrorException;
import net.jcflorezr.model.endpoint.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class EndpointErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> processBadRequestExceptions(BadRequestException e, WebRequest request) {
        return processException(e, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<Object> processInternalServerErrorException(InternalServerErrorException e, WebRequest request) {
        return processException(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> processException(AudioSplitterCustomException e, WebRequest request, HttpStatus httpStatus) {
        ErrorResponse errorResponse = new ErrorResponse(e);
        return handleExceptionInternal(e, errorResponse, headersAsJsonTypeResponse(), httpStatus, request);
    }

    private HttpHeaders headersAsJsonTypeResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}

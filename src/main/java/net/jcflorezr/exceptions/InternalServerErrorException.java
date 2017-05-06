package net.jcflorezr.exceptions;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InternalServerErrorException extends AudioSplitterCustomException {

    private static final String ELEMENTS_DELIMITER = ",";

    private String exceptionClassName;
    private String message;
    private String simplifiedStackTrace;

    public InternalServerErrorException() {
    }

    public InternalServerErrorException(Exception e) {
        exceptionClassName = e.getClass().getName();
        message = e.getMessage();
        simplifiedStackTrace = getSimplifiedStackTrace(e.getStackTrace());
    }

    private String getSimplifiedStackTrace(StackTraceElement[] stackTraceElements) {
        AtomicInteger counter = new AtomicInteger(1);
        return Stream.of(stackTraceElements)
                .collect(Collectors.groupingBy(
                            stackTraceElement -> counter + "_" + stackTraceElement.getClassName(),
                            LinkedHashMap::new,
                            Collectors.mapping(stackTraceElement -> String.valueOf(stackTraceElement.getLineNumber()),
                                                                    Collectors.joining(ELEMENTS_DELIMITER))))
        .toString();
    }

    public String getExceptionClassName() {
        return exceptionClassName;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getSimplifiedStackTrace() {
        return simplifiedStackTrace;
    }

    @Override
    public String toString() {
        return "InternalServerErrorException{" +
                "exceptionClassName='" + exceptionClassName + '\'' +
                ", message='" + message + '\'' +
                ", simplifiedStackTrace='" + simplifiedStackTrace + '\'' +
                '}';
    }
}

package biz.source_code.dsp.exceptions;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InternalServerErrorException extends RuntimeException {

    String exceptionClassName;
    String message;
    String simplifiedStackTrace;

    public InternalServerErrorException(Exception e) {
        exceptionClassName = e.getClass().getName();
        message = e.getMessage();
        simplifiedStackTrace = getSimplifiedStackTrace(e.getStackTrace());
    }

    private String getSimplifiedStackTrace(StackTraceElement[] stackTraceElements) {
        AtomicInteger counter = new AtomicInteger(1);
        return Stream.of(stackTraceElements)
                .collect(Collectors.groupingBy(s -> counter + "_" + s.getClassName(),
                                    Collectors.mapping(s -> String.valueOf(s.getLineNumber()),
                                    Collectors.joining(","))))
        .toString();
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

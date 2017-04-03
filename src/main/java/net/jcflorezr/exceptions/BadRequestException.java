package net.jcflorezr.exceptions;

public class BadRequestException extends AudioSplitterCustomException {

    private String message;
    private String suggestion;

    public BadRequestException(String message) {
        this.message = message;
    }

    public BadRequestException(String message, String suggestion) {
        this.message = message;
        this.suggestion = suggestion;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public String toString() {
        return "BadRequestException{" +
                "message='" + message + '\'' +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }
}

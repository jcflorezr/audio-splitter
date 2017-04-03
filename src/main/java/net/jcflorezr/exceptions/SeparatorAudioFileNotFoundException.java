package net.jcflorezr.exceptions;

public class SeparatorAudioFileNotFoundException extends RuntimeException {

    @Override
    public String getMessage() {
        return "The audio separator file set in the properties file was not found.";
    }

}

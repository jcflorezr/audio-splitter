package net.jcflorezr.exceptions;

import java.nio.file.Path;

public class AudioFileLocationException extends BadRequestException {

    public AudioFileLocationException(String message, String suggestion) {
        super(message, suggestion);
    }

    public static AudioFileLocationException audioFileDoesNotExist(Path audioFileName) {
        return new AudioFileLocationException("The audio file '" + audioFileName + "' does not exist.", null);
    }

    public static AudioFileLocationException audioFileShouldNotBeDirectory(Path audioFileName) {
        return new AudioFileLocationException("'" + audioFileName + "' should be a file, not a directory.", null);
    }

    public static AudioFileLocationException outputDirectoryDoesNotExist(Path outputAudioClipsPath) {
        return new AudioFileLocationException("The directory '" + outputAudioClipsPath + "' does not exist.", null);
    }

    public static AudioFileLocationException sameAudioFileAndOutputDirectoryLocation() {
        return new AudioFileLocationException("The audio file location cannot be the same as the " +
                "output audio clips location.", null);
    }

    public static AudioFileLocationException emptyAudioFileLocationObject() {
        String suggestion = "Example of body request: {\"audioFileName\": \"/any-audio-file.wav\", \"outputAudioClipsDirectoryPath\": \"/any-output-directory/\"}";
        return new AudioFileLocationException("There is no body in the current request.", suggestion);
    }

    public static AudioFileLocationException mandatoryFieldsException() {
        String suggestion = "Mandatory fields are: [audioFileName, outputAudioClipsDirectoryPath]";
        return new AudioFileLocationException("There are empty mandatory fields.", suggestion);
    }
}

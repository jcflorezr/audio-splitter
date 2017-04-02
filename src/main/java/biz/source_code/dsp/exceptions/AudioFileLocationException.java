package biz.source_code.dsp.exceptions;

import java.nio.file.Path;

public class AudioFileLocationException extends BadRequestException {

    private String message;
    private String suggestion;

    public AudioFileLocationException(String message, String suggestion) {
        this.message = message;
        this.suggestion = suggestion;
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

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getSuggestion() {
        return suggestion;
    }
}

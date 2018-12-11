package net.jcflorezr.exception;

class AudioFileLocationException(
        message: String,
        suggestion: String? = null
) : BadRequestException(message, suggestion) {
    companion object {

        fun audioFileDoesNotExist(audioFileName: String): AudioFileLocationException {
            return AudioFileLocationException(
                    message = "The audio file '$audioFileName' does not exist."
            )
        }

        fun audioFileShouldNotBeDirectory(audioFileName: String): AudioFileLocationException {
            return AudioFileLocationException(
                    message = "'$audioFileName' should be a file, not a directory."
            )
        }

        fun outputDirectoryDoesNotExist(outputAudioClipsPath: String): AudioFileLocationException {
            return AudioFileLocationException(
                    message = "The directory '$outputAudioClipsPath' does not exist."
            )
        }

        fun sameAudioFileAndOutputDirectoryLocation(): AudioFileLocationException {
            return AudioFileLocationException(
                    message = "The audio file location cannot be the same as the " + "output audio clips location."
            )
        }

        fun emptyAudioFileLocationObject(): AudioFileLocationException {
            return AudioFileLocationException(
                    message = "There is no body in the current endpoint.",
                    suggestion = "Example of body endpoint: {\"audioFileName\": \"/any-audio-file.wav\", \"outputAudioClipsDirectoryPath\": \"/any-output-directory/\"}"
            )
        }

        fun mandatoryFieldsMissingException(): AudioFileLocationException {
            return AudioFileLocationException(
                    message = "There are empty mandatory fields.",
                    suggestion = "Mandatory fields are: [audioFileName, outputAudioClipsDirectoryPath]"
            )
        }
    }
}

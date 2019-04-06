package net.jcflorezr.exception

import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignal

class SourceAudioFileValidationException(
    message: String,
    errorCode: String,
    suggestion: String? = null
) : BadRequestException(message = message, errorCode = errorCode, suggestion = suggestion) {
    companion object {

        fun audioFileDoesNotExist(audioFileName: String) =
            SourceAudioFileValidationException(
                errorCode = "audio_file_not_found",
                message = "Source audio file '$audioFileName' does not exist."
            )

        fun audioFileDoesNotExistInBucket(audioFileName: String) =
            SourceAudioFileValidationException(
                errorCode = "audio_file_not_found_in_bucket",
                message = "Source audio file '$audioFileName' does not exist in the bucket."
            )

        fun audioFileShouldNotBeDirectory(audioFileName: String) =
            SourceAudioFileValidationException(
                errorCode = "audio_file_path_is_a_directory_path",
                message = "Source audio file: '$audioFileName' should be a file, not a directory."
            )

        fun mandatoryFieldsMissingException() = SourceAudioFileValidationException(
            message = "There are empty mandatory fields.",
            errorCode = "missing_mandatory_fields",
            suggestion = "Mandatory fields are: [audioFileName]"
        )

        fun existingAudioSplitProcessException(audioFileName: String) = SourceAudioFileValidationException(
            message = "There is already an audio split process for audio file '$audioFileName'.",
            errorCode = "existing_audio_split_process",
            suggestion = "Please wait until you get notified that the current audio split process " +
                "has finished and then send the audio file again"
        )
    }
}

class SignalException(message: String) : RuntimeException(message) {
    companion object {

        fun signalPartNotStoredException(audioSignal: AudioSignal) =
            InternalServerErrorException(
                errorCode = "signal_part_was_not_stored",
                ex = SignalException(
                    message = "Signal part for ${audioSignal.audioFileName} could not be persisted for later usage. \n" +
                        "Signal part details ----> $audioSignal"
                )
            )
    }
}

class AudioClipException(message: String) : RuntimeException(message) {
    companion object {

        fun noPreviousClipProcessedException() =
            InternalServerErrorException(
                errorCode = "no_audio_clip_processed_before_finishing",
                ex = SignalException(
                    message = "Clip generation process is about to end and there is no previous " +
                        "clip processed before sending the last audio clip to be processed"
                )
            )

        fun moreThanOnePreviousClipsProcessedException(numOfPreviousProcessedClips: Int) =
            InternalServerErrorException(
                errorCode = "more_than_one_audio_clips_processed_before_finishing",
                ex = SignalException(
                    message = "Clip generation process is about to end and there are $numOfPreviousProcessedClips previous " +
                        "clips processed before sending the last audio clip to be processed"
                )
            )

        fun noAudioSignalsFoundForCreatingAudioClip(audioClipInfo: AudioClipInfo) =
            InternalServerErrorException(
                errorCode = "no_audio_signals_found_for_creating_audio_clip",
                ex = SignalException(
                    message = "No audio signals were found in database to start creating the audio clip: $audioClipInfo"
                )
            )
    }
}

class ActorException(message: String) : RuntimeException(message) {
    companion object {

        fun unknownActorOperationException(actorClass: Class<*>, actorOperationClass: Class<*>) =
            InternalServerErrorException(
                errorCode = "unknown_operation_for_actor",
                ex = ActorException(
                    message = "Operation $actorOperationClass is not supported by $actorClass."
                )
            )
    }
}
package net.jcflorezr.audio.splitter.domain.exception

// import net.jcflorezr.model.AudioClipInfo
// import net.jcflorezr.model.AudioSignal

class SignalException(message: String) : RuntimeException(message) {
    companion object {

        /*
        fun signalPartNotStoredException(audioSignal: AudioSignal) =
            InternalServerErrorException(
                errorCode = "signal_part_was_not_stored",
                ex = SignalException(
                    message = "Signal part for ${audioSignal.audioFileName} could not be persisted for later usage. \n" +
                        "Signal part details ----> $audioSignal"
                )
            )
         */
    }
}

class AudioClipException(message: String) : RuntimeException(message) {
    companion object {

        fun noPreviousClipProcessedException() =
                InternalServerErrorException(
                        errorCode = "no_audio_clip_processed_before_finishing",
                        exception = SignalException(
                                message = "Clip generation process is about to end and there is no previous " +
                                        "clip processed before sending the last audio clip to be processed"
                        )
                )

        fun moreThanOnePreviousClipsProcessedException(numOfPreviousProcessedClips: Int) =
                InternalServerErrorException(
                        errorCode = "more_than_one_audio_clips_processed_before_finishing",
                        exception = SignalException(
                                message = "Clip generation process is about to end and there are $numOfPreviousProcessedClips previous " +
                                        "clips processed before sending the last audio clip to be processed"
                        )
                )
/*
        fun noAudioSignalsFoundForCreatingAudioClip(audioClipInfo: AudioClipInfo) =
            InternalServerErrorException(
                errorCode = "no_audio_signals_found_for_creating_audio_clip",
                ex = SignalException(
                    message = "No audio signals were found in database to start creating the audio clip: $audioClipInfo"
                )
            )
 */
    }
}

class ActorException(message: String) : RuntimeException(message) {
    companion object {

        fun unknownActorOperationException(actorClass: Class<*>, actorOperationClass: Class<*>) =
                InternalServerErrorException(
                        errorCode = "unknown_operation_for_actor",
                        exception = ActorException(
                                message = "Operation $actorOperationClass is not supported by $actorClass."
                        )
                )
    }
}
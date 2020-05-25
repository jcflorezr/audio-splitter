package net.jcflorezr.transcriber.audio.transcriber.adapters.di.repositories.audiotranscriptions

import net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.audiotranscriptions.AudioTranscriptionsCassandraDaoDI
import net.jcflorezr.transcriber.audio.transcriber.adapters.repositories.audiotranscriptions.DefaultAudioTranscriptionsRepository

class DefaultAudioTranscriptionsRepositoryDI(
    audioTranscriptionsCassandraDaoDI: AudioTranscriptionsCassandraDaoDI
) {

    val defaultAudioTranscriptionsRepository = DefaultAudioTranscriptionsRepository(
        audioTranscriptionsCassandraDaoDI.audioTranscriptionsCassandraDao
    )
}

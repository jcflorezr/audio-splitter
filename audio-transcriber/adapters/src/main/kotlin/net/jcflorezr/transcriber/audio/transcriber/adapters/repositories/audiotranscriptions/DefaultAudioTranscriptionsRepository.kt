package net.jcflorezr.transcriber.audio.transcriber.adapters.repositories.audiotranscriptions

import net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions.AudioTranscriptionsCassandraDao
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.repositories.audiotranscriptions.AudioTranscriptionsRepository

class DefaultAudioTranscriptionsRepository(
    private val audioTranscriptionsCassandraDao: AudioTranscriptionsCassandraDao
) : AudioTranscriptionsRepository {

    override suspend fun findBy(sourceAudioFileName: String): List<AudioTranscription> =
        audioTranscriptionsCassandraDao.findBy(sourceAudioFileName).map { it.translate() }

    override suspend fun save(audioTranscription: AudioTranscription) {
        audioTranscriptionsCassandraDao.save(audioTranscriptionsCassandraDao.toRecord(audioTranscription))
    }
}

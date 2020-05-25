package net.jcflorezr.transcriber.audio.transcriber.domain.ports.repositories.audiotranscriptions

import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription

interface AudioTranscriptionsRepository {

    suspend fun findBy(sourceAudioFileName: String): List<AudioTranscription>

    suspend fun save(audioTranscription: AudioTranscription)
}

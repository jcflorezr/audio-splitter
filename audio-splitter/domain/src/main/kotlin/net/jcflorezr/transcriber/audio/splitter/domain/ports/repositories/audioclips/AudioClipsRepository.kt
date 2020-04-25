package net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.audioclips

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip

interface AudioClipsRepository {

    suspend fun findBy(sourceAudioFileName: String, hours: Int, minutes: Int, seconds: Int, tenthsOfSecond: Int): AudioClip

    suspend fun findBy(sourceAudioFileName: String): List<AudioClip>

    suspend fun save(audioClip: AudioClip)
}
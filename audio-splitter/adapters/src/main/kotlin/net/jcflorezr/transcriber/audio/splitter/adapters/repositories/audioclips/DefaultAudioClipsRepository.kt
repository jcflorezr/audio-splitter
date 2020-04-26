package net.jcflorezr.transcriber.audio.splitter.adapters.repositories.audioclips

import net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips.AudioClipsCassandraDao
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.audioclips.AudioClipsRepository

class DefaultAudioClipsRepository(
    private val audioClipsCassandraDao: AudioClipsCassandraDao
) : AudioClipsRepository {

    override suspend fun findBy(sourceAudioFileName: String, hours: Int, minutes: Int, seconds: Int, tenthsOfSecond: Int): AudioClip =
        audioClipsCassandraDao.findBy(sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond).translate()

    override suspend fun findBy(sourceAudioFileName: String): List<AudioClip> =
        audioClipsCassandraDao.findBy(sourceAudioFileName).map { it.translate() }.toList()

    override suspend fun save(audioClip: AudioClip) {
        audioClipsCassandraDao.save(audioClipsCassandraDao.toRecord(audioClip))
    }
}

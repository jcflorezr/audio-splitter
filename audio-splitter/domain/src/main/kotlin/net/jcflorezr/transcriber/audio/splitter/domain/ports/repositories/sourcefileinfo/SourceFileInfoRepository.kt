package net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo

interface SourceFileInfoRepository {

    suspend fun findBy(audioFileName: String): AudioSourceFileInfo

    suspend fun save(audioSourceFileInfo: AudioSourceFileInfo)
}
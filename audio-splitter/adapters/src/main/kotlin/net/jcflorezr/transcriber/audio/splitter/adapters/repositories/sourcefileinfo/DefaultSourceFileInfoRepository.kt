package net.jcflorezr.transcriber.audio.splitter.adapters.repositories.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo.SourceFileInfoCassandraDao
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository

class DefaultSourceFileInfoRepository(
    private val sourceFileInfoCassandraDao: SourceFileInfoCassandraDao
) : SourceFileInfoRepository {

    override suspend fun findBy(audioFileName: String) = sourceFileInfoCassandraDao.findBy(audioFileName).translate()

    override suspend fun save(audioSourceFileInfo: AudioSourceFileInfo) {
        sourceFileInfoCassandraDao.save(
            sourceFileInfoCassandraRecord = sourceFileInfoCassandraDao.toRecord(audioSourceFileInfo))
    }
}

package net.jcflorezr.transcriber.audio.splitter.adapters.di.repositories.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.sourcefileinfo.SourceFileInfoCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.sourcefileinfo.DefaultSourceFileInfoRepository

class DefaultSourceFileInfoRepositoryDI(
    sourceFileInfoCassandraDaoDI: SourceFileInfoCassandraDaoDI
) {

    val defaultSourceFileInfoRepository = DefaultSourceFileInfoRepository(
        sourceFileInfoCassandraDaoDI.sourceFileInfoCassandraDao
    )
}

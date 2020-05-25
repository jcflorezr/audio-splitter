package net.jcflorezr.transcriber.audio.splitter.adapters.di.repositories.audioclips

import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audioclips.AudioClipsCassandraDaoDI
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.audioclips.DefaultAudioClipsRepository

class DefaultAudioClipsRepositoryDI(
    audioClipsCassandraDaoDI: AudioClipsCassandraDaoDI
) {

    val defaultAudioClipsRepository = DefaultAudioClipsRepository(
        audioClipsCassandraDaoDI.audioClipsCassandraDao
    )
}

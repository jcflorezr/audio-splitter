package net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.application

interface AudioSourceFileInfoService {
    suspend fun extractAudioInfoFromSourceFile(audioFileName: String)
}

package net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audiosegments.application

import java.io.File
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo

interface AudioSegmentsService {
    suspend fun generateAudioSegments(audioContentInfo: AudioContentInfo, audioFile: File)
}

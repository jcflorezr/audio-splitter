package net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audiosegments.application

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import java.io.File

interface AudioSegmentsService {
    suspend fun generateAudioSegments(audioContentInfo: AudioContentInfo, audioFile: File)
}
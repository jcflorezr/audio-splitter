package net.jcflorezr.transcriber.audio.splitter.domain.events.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audiosegments.application.AudioSegmentsService
import net.jcflorezr.transcriber.core.domain.EventHandler
import java.io.File

class AudioSourceFileInfoGeneratedHandler(
    private val audioSegmentsService: AudioSegmentsService
) : EventHandler<AudioSourceFileInfoGenerated> {

    override suspend fun execute(event: AudioSourceFileInfoGenerated) {
        val audioSourceFileInfo = event.audioSourceFileInfo
        val audioFile = File(audioSourceFileInfo.convertedAudioFile ?: audioSourceFileInfo.originalAudioFile)
        audioSegmentsService.generateAudioSegments(audioSourceFileInfo.audioContentInfo, audioFile)
    }
}
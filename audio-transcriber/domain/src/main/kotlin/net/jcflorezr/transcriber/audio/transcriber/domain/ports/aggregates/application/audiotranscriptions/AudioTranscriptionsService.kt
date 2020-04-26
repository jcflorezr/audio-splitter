package net.jcflorezr.transcriber.audio.transcriber.domain.ports.aggregates.application.audiotranscriptions

import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo

interface AudioTranscriptionsService {
    suspend fun transcribe(audioClipFileInfo: AudioClipFileInfo)
}

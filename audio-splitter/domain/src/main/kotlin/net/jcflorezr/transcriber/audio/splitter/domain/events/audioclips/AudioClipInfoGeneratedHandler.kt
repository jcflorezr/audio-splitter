package net.jcflorezr.transcriber.audio.splitter.domain.events.audioclips

import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioClipsFilesGenerator
import net.jcflorezr.transcriber.core.domain.EventHandler

class AudioClipInfoGeneratedHandler(
    private val audioClipsFilesGenerator: AudioClipsFilesGenerator
) : EventHandler<AudioClipInfoGenerated> {

    override suspend fun execute(event: AudioClipInfoGenerated) {
        audioClipsFilesGenerator.generateAudioClipFile(event.audioClip)
    }
}
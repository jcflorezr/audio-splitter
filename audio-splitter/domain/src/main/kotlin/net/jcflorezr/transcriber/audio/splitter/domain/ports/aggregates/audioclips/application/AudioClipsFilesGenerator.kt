package net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip

interface AudioClipsFilesGenerator {
    suspend fun generateAudioClipFile(audioClipInfo: AudioClip)
}
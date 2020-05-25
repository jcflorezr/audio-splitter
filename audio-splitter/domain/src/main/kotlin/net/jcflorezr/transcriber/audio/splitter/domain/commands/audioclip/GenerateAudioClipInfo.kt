package net.jcflorezr.transcriber.audio.splitter.domain.commands.audioclip

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.events.audioclips.AudioClipInfoGenerated
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.audioclips.AudioClipsRepository
import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.domain.EventDispatcher

class GenerateAudioClipInfo(
    private val audioClipsRepository: AudioClipsRepository,
    private val eventDispatcher: EventDispatcher
) : Command<AudioClip> {

    override suspend fun execute(aggregateRoot: AudioClip) {
        audioClipsRepository.save(audioClip = aggregateRoot)
        eventDispatcher.publish(AudioClipInfoGenerated(aggregateRoot))
    }
}

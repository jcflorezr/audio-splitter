package net.jcflorezr.transcriber.audio.splitter.domain.commands.audioclip

import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.domain.EventDispatcher
import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo
import net.jcflorezr.transcriber.core.domain.events.audioclips.AudioClipFileGenerated

class GenerateAudioClipFile(
    private val eventDispatcher: EventDispatcher
) : Command<AudioClipFileInfo> {

    override suspend fun execute(aggregateRoot: AudioClipFileInfo) {
        eventDispatcher.publish(AudioClipFileGenerated(aggregateRoot))
    }
}
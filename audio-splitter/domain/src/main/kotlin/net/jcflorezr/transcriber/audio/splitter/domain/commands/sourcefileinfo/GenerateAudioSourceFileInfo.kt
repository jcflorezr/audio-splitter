package net.jcflorezr.transcriber.audio.splitter.domain.commands.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.events.sourcefileinfo.AudioSourceFileInfoGenerated
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.domain.Event
import net.jcflorezr.transcriber.core.domain.EventDispatcher

class GenerateAudioSourceFileInfo(
    private val sourceFileInfoRepository: SourceFileInfoRepository,
    private val eventDispatcher: EventDispatcher
) : Command<AudioSourceFileInfo> {

    override suspend fun execute(aggregateRoot: AudioSourceFileInfo) {
        sourceFileInfoRepository.save(audioSourceFileInfo = aggregateRoot)
        sourceFileInfoRepository.findBy(audioFileName = aggregateRoot.originalAudioFile)
        eventDispatcher.publish(AudioSourceFileInfoGenerated(aggregateRoot))
    }
}
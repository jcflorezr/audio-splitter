package net.jcflorezr.transcriber.audio.splitter.domain.commands.audiosegments

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegments
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.events.audiosegments.AudioSegmentsGenerated
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.audiosegments.AudioSegmentsRepository
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.domain.EventDispatcher

class GenerateAudioSegment(
    private val sourceFileInfoRepository: SourceFileInfoRepository,
    private val audioSegmentsRepository: AudioSegmentsRepository,
    private val eventDispatcher: EventDispatcher
) : Command<AudioSegment> {

    private val sourceAudioFilesInfo: MutableMap<String, AudioSourceFileInfo> = mutableMapOf()
    private val sourceAudioFilesSegments: MutableMap<String, MutableList<BasicAudioSegment>> = mutableMapOf()

    override suspend fun execute(aggregateRoot: AudioSegment) {
        val sourceAudioFileName = aggregateRoot.sourceAudioFileName
        audioSegmentsRepository.save(audioSegment = aggregateRoot)
        audioSegmentsRepository.findBy(sourceAudioFileName, aggregateRoot.segmentStartInSeconds)

        val numberOfExpectedAudioSegments = sourceAudioFilesInfo.getOrPut(key = sourceAudioFileName) {
            sourceFileInfoRepository.findBy(sourceAudioFileName)
        }.audioContentInfo.numOfAudioSegments

        sourceAudioFilesSegments.getOrPut(key = sourceAudioFileName) { mutableListOf() }
            .also { audioSegments -> audioSegments.add(aggregateRoot.toBasicAudioSegment()) }
            .takeIf { it.size == numberOfExpectedAudioSegments }
            ?.let { audioSegments -> eventDispatcher.publish(AudioSegmentsGenerated(BasicAudioSegments(audioSegments))) }
    }
}
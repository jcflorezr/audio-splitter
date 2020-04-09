package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.ActiveSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.CurrentSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.CurrentSegmentWithNoisyBackground
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import net.jcflorezr.transcriber.core.domain.Command

interface AudioClipsInfoService {
    suspend fun generateActiveSegments(audioSegments: List<BasicAudioSegment>)
}

class AudioClipsInfoServiceImpl(
    private val command: Command,
    private val sourceFileInfoRepository: SourceFileInfoRepository
) : AudioClipsInfoService {

    override suspend fun generateActiveSegments(audioSegments: List<BasicAudioSegment>) {
        var audioClip = AudioClip.createNew()
        audioSegments.asSequence()
            .foldIndexed(CurrentSegment.createNew()) { i, segmentProcessed, currentSegment ->
                segmentProcessed.process(audioSegment = currentSegment, isLastSegment = i == audioSegments.size - 1)
                .run {
                    when {
                        activeSegmentEnd > activeSegmentStart -> {
                            audioClip = audioClip.generateActiveSegmentInfo(
                                activeSegmentStart, activeSegmentEnd, currentSegment.sourceAudioFileName)
                            this.copy(activeSegmentEnd = 0)
                        }
                        segmentWithNoisyBackgroundDetected -> {
                            val segmentLength = currentSegment.segmentEnd - currentSegment.segmentStart
                            generateActiveSegmentsWithNoisyBackground(
                                audioSegments = audioSegments,
                                fromIndex = activeSegmentStart / segmentLength,
                                toIndex = i,
                                _audioClip = audioClip)
                            audioClip = audioClip.flush()
                            this.copy(segmentWithNoisyBackgroundDetected = false)
                        }
                        else -> this
                    }
                }
            }
        audioClip.takeIf { !it.isFlushed() }?.also { processLastClip(audioClip = it) }
    }

    private suspend fun generateActiveSegmentsWithNoisyBackground(
        audioSegments: List<BasicAudioSegment>,
        fromIndex: Int,
        toIndex: Int,
        _audioClip: AudioClip
    ) {
        var audioClip = _audioClip
        val initialCurrentSegment = CurrentSegmentWithNoisyBackground.createNew(audioSegments, fromIndex)
        audioSegments.subList(fromIndex, toIndex).asSequence()
            .foldIndexed(initialCurrentSegment) { i, segmentProcessed, currentSegment ->
                segmentProcessed.process(audioSegment = currentSegment, currentIndex = i)
                .run {
                    when {
                        activeSegmentEnd > activeSegmentStart -> {
                            audioClip = audioClip.generateActiveSegmentInfo(
                                activeSegmentStart, activeSegmentEnd, currentSegment.sourceAudioFileName)
                            this.copy(activeSegmentEnd = 0)
                        }
                        else -> this
                    }
                }
            }
        audioClip.takeIf { !it.isFlushed() }?.also { processLastClip(audioClip = it) }
    }

    private suspend fun AudioClip.generateActiveSegmentInfo(
        startActiveSegment: Int,
        endActiveSegment: Int,
        audioFileName: String
    ): AudioClip {
        val sourceFileInfo = sourceFileInfoRepository.findBy(audioFileName)
        val activeSegment = ActiveSegment.createNew(
            sourceAudioFileName = audioFileName,
            segmentStart = startActiveSegment,
            segmentEnd = endActiveSegment,
            audioContentInfo = sourceFileInfo.audioContentInfo)
        val audioClip = processActiveSegment(activeSegment)
        return when {
            audioClip.duration > 0 -> {
                coroutineScope { launch { command.execute(aggregateRoot = audioClip) } }
                audioClip.reset()
            }
            else -> audioClip
        }
    }

    private suspend fun processLastClip(audioClip: AudioClip) =
        coroutineScope { launch { command.execute(aggregateRoot = audioClip.finish()) } }
}
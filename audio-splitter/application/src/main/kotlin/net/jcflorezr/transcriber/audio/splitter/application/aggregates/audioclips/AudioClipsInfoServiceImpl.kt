package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.activesegment.Segment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.activesegment.SegmentInNoise
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.activesegment.SegmentInSilence
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioClipsInfoService
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import net.jcflorezr.transcriber.core.domain.Command

class AudioClipsInfoServiceImpl(
    private val sourceFileInfoRepository: SourceFileInfoRepository,
    private val command: Command<AudioClip>
) : AudioClipsInfoService {

    override suspend fun generateActiveSegments(audioSegments: List<BasicAudioSegment>) {
        val audioContentInfo = sourceFileInfoRepository.findBy(audioSegments.first().sourceAudioFileName).audioContentInfo
        audioSegments
            .fold(Segment.createNewSegmentInSilence(audioSegments, audioContentInfo) as Segment) { segmentProcessed, _ ->
                segmentProcessed.process().let { segment ->
                    when (segment) {
                        is SegmentInNoise -> generateActiveSegmentsWithNoisyBackground(segment)
                        else -> (segment as SegmentInSilence).copy(audioClip = segment.sendAudioClipIfReady())
                    }
                }
            }.also { it.processLastClip() }
    }

    private suspend fun generateActiveSegmentsWithNoisyBackground(initialSegment: SegmentInNoise): Segment =
        initialSegment.audioSegments.subList(initialSegment.fromIndex, initialSegment.toIndex)
            .fold(initialSegment) { segmentProcessed, _ ->
                segmentProcessed.process().let { it.copy(audioClip = it.sendAudioClipIfReady()) }
            }
            .also { it.processLastClip() }
            .segmentInSilence

    private suspend fun Segment.sendAudioClipIfReady(): AudioClip =
        when {
            audioClip.duration > 0 -> {
                coroutineScope { launch { command.execute(aggregateRoot = this@sendAudioClipIfReady.audioClip) } }
                audioClip.reset()
            }
            else -> audioClip
        }

    private suspend fun Segment.processLastClip() {
        if (!audioClip.isFlushed()) {
            coroutineScope { launch { command.execute(aggregateRoot = this@processLastClip.audioClip.finish()) } }
        }
    }
}

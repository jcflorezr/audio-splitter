package net.jcflorezr.transcriber.audio.splitter.adapters.ports.audiosegments

import java.io.File
import java.util.stream.IntStream
import javax.sound.sampled.AudioInputStream
import kotlin.streams.asSequence
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegmentRms
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioFrameProcessor
import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.exception.AudioSegmentException

class AudioFrameProcessorImpl(
    private val command: Command<AudioSegment>
) : AudioFrameProcessor {

    override suspend fun processAudioFrame(
        audioContentInfo: AudioContentInfo,
        stream: AudioInputStream,
        audioFile: File
    ) = coroutineScope<Unit> {
        audioContentInfo.run {
            generateSequence(0) { framesStart ->
                val requiredFrames = calculateCurrentRequiredFrames(framesStart)
                val (frameBytesRead, framesBytesArray) = stream.readBytes(
                    initialPosition = framesStart, frameSize = frameSize, bytesArrayLength = requiredFrames * frameSize
                )
                launch { processAudioSegment(framesStart, framesBytesArray, audioFile, this@run) }
                calculateNextIteration(framesStart, frameBytesRead)
            }
                .takeWhile { frameStart -> frameStart < exactTotalFrames }
                .count()
        }
    }

    private suspend fun processAudioSegment(
        framesStart: Int,
        framesBytesArray: ByteArray,
        audioFile: File,
        audioContentInfo: AudioContentInfo
    ) = coroutineScope {
        val numSegments = audioContentInfo.calculateCurrentNumOfAudioSegments(framesStart)
        IntStream.range(0, numSegments).asSequence()
            .forEach { segmentStart ->
                val audioSegmentBytes = async {
                    coroutineScope { AudioSegment.extractAudioSegmentByteArray(segmentStart, framesBytesArray, audioContentInfo) }
                }
                val audioSegmentSignal = async {
                    coroutineScope { AudioSegment.generateAudioSegmentSignal(segmentStart, framesBytesArray, audioContentInfo) }
                }
                val audioSegment = AudioSegment.createNew(
                    segmentStart = framesStart + (segmentStart * audioContentInfo.audioSegmentLength),
                    sourceAudioFileName = audioFile.nameWithoutExtension,
                    audioContentInfo = audioContentInfo,
                    audioSegmentBytes = audioSegmentBytes.await(),
                    audioSegmentRms = AudioSegmentRms.createNew(audioSegmentSignal.await())
                )
                launch { command.execute(audioSegment) }
            }
    }

    private fun AudioInputStream.readBytes(
        initialPosition: Int,
        frameSize: Int,
        bytesArrayLength: Int
    ): Pair<Int, ByteArray> {
        val bytesBuffer = ByteArray(bytesArrayLength)
        val bytesRead = read(bytesBuffer)
        return when {
            bytesRead <= 0 ->
                throw AudioSegmentException.unexpectedEndOfFile(bytesArrayLength, initialPosition, frameSize)
            bytesRead % frameSize != 0 ->
                throw AudioSegmentException.incorrectSignalLengthForFrameSize(bytesArrayLength, bytesRead, frameSize)
            else -> bytesRead to bytesBuffer
        }
    }

    private fun AudioContentInfo.calculateCurrentRequiredFrames(framesStart: Int) =
        framesPerSecond.takeIf { framesStart < totalFramesBySeconds } ?: remainingFrames

    private fun AudioContentInfo.calculateNextIteration(framesStart: Int, frameBytesRead: Int) =
        when (val framesRead = frameBytesRead / frameSize) {
            framesPerSecond -> framesStart + framesRead
            remainingFrames -> exactTotalFrames
            else -> totalFramesBySeconds
        }

    private fun AudioContentInfo.calculateCurrentNumOfAudioSegments(framesStart: Int) =
        audioSegmentsPerSecond.takeIf { framesStart < totalFramesBySeconds } ?: remainingAudioSegments
}

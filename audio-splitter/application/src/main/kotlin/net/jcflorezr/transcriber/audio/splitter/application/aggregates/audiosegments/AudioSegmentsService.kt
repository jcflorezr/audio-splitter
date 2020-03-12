package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegmentBytes
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegmentRms
import net.jcflorezr.transcriber.audio.splitter.domain.exception.AudioSegmentException
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.util.AudioBytesUnPacker
import net.jcflorezr.transcriber.core.domain.Command
import java.io.File
import java.util.stream.IntStream
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlin.streams.asSequence

interface AudioSegmentsService {
    suspend fun generateAudioSegments(audioContentInfo: AudioContentInfo, audioFile: File)
}

class AudioSegmentsServiceImpl(private val command: Command) : AudioSegmentsService {

    override suspend fun generateAudioSegments(audioContentInfo: AudioContentInfo, audioFile: File) =
        withContext<Unit>(Dispatchers.IO) {
            AudioSystem.getAudioInputStream(audioFile)
                .use { stream -> audioContentInfo.processAudioFrame(stream, audioFile) }
        }

    private suspend fun AudioContentInfo.processAudioFrame(stream: AudioInputStream, audioFile: File) = coroutineScope {
        generateSequence(0) { framesStart ->
            val requiredFrames = calculateCurrentRequiredFrames(framesStart)
            val (frameBytesRead, framesBytesArray) = stream.readBytes(
                initialPosition = framesStart, frameSize =  frameSize, bytesArrayLength =  requiredFrames * frameSize)
            launch {
                this@processAudioFrame.processAudioSegment(framesStart, framesBytesArray, audioFile)
            }
            calculateNextIteration(framesStart, frameBytesRead)
        }
        .takeWhile { frameStart -> frameStart < exactTotalFrames }
        .count()
    }

    private suspend fun AudioContentInfo.processAudioSegment(
        framesStart: Int,
        framesBytesArray: ByteArray,
        audioFile: File
    ) = coroutineScope {
        val numSegments = calculateCurrentNumOfAudioSegments(framesStart)
        IntStream.range(0, numSegments).asSequence()
            .forEach { segmentStart ->
                val audioSegmentBytes = async { extractAudioSegmentByteArray(segmentStart, framesBytesArray) }
                val audioSegmentSignal = async { generateAudioSegmentSignal(segmentStart, framesBytesArray) }
                val audioSegment = AudioSegment.createNew(
                    initialPosition = framesStart + (segmentStart * audioSegmentLength),
                    audioFileName = audioFile.nameWithoutExtension,
                    audioContentInfo = this@processAudioSegment,
                    audioSegmentBytes = audioSegmentBytes.await(),
                    audioSegmentRms = AudioSegmentRms.createNew(audioSegmentSignal.await()))
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

    private suspend fun AudioContentInfo.extractAudioSegmentByteArray(
        segmentStart: Int,
        byteArray: ByteArray
    ) = coroutineScope {
        AudioSegmentBytes.of(
            bytes = byteArray,
            from = segmentStart * audioSegmentLengthInBytes,
            to = (segmentStart * audioSegmentLengthInBytes) + audioSegmentLengthInBytes)
    }

    private suspend fun AudioContentInfo.generateAudioSegmentSignal(
        segmentStart: Int,
        byteArray: ByteArray
    ) = coroutineScope {
        AudioBytesUnPacker.generateAudioSignal(
            audioContentInfo = this@generateAudioSegmentSignal,
            bytesBuffer = byteArray,
            from = segmentStart * audioSegmentLength,
            to = (segmentStart * audioSegmentLength) + audioSegmentLength)
    }
}
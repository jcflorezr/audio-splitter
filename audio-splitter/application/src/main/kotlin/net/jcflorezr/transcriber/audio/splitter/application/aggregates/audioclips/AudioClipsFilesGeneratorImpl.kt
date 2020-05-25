package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioFormatEncodings
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioClipsFilesGenerator
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.audiosegments.AudioSegmentsRepository
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import net.jcflorezr.transcriber.audio.splitter.domain.util.AudioBytesPacker
import net.jcflorezr.transcriber.audio.splitter.domain.util.AudioBytesUnPacker
import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo
import net.jcflorezr.transcriber.core.util.SupportedAudioFormats

class AudioClipsFilesGeneratorImpl(
    private val audioSegmentsRepository: AudioSegmentsRepository,
    private val sourceFileInfoRepository: SourceFileInfoRepository,
    private val clipsFilesDirectory: String,
    private val command: Command<AudioClipFileInfo>
) : AudioClipsFilesGenerator {

    companion object {
        private val FLAC_TYPE = SupportedAudioFormats.FLAC.fileType
        private val FLAC_EXTENSION = SupportedAudioFormats.FLAC.extension
    }

    override suspend fun generateAudioClipFile(audioClipInfo: AudioClip) = withContext<Unit>(Dispatchers.IO) {
        audioClipInfo.run {
            val firstSegment = activeSegments.first()
            val lastSegment = activeSegments.last()
            val audioContentInfo = sourceFileInfoRepository.findBy(firstSegment.sourceAudioFileName).audioContentInfo
            audioSegmentsRepository.findSegmentsRange(
                    sourceAudioFileName, firstSegment.segmentStartInSeconds, lastSegment.segmentEndInSeconds)
                .map { it.audioSegmentBytes.bytes }
                .reduce { currentSegmentBytes, nextSegmentBytes -> currentSegmentBytes + nextSegmentBytes }
                .let { byteArray ->
                    val audioClipSignal = AudioBytesUnPacker.generateAudioSignal(audioContentInfo, byteArray)
                    val audioClipInputStream = audioContentInfo.getAudioInputStreamForPackingBytes(audioClipSignal)
                    val pathToStoreClipFile = "$clipsFilesDirectory/$sourceAudioFileName"
                    File(pathToStoreClipFile).mkdirs()
                    val audioClipFile = File("$pathToStoreClipFile/${audioClipFileName()}.$FLAC_EXTENSION")
                    AudioSystem.write(audioClipInputStream, FLAC_TYPE, audioClipFile)
                }
            val audioClipFileInfo =
                AudioClipFileInfo(sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond, audioClipFileName, FLAC_EXTENSION)
            launch { command.execute(aggregateRoot = audioClipFileInfo) }
        }
    }

    private fun AudioContentInfo.getAudioInputStreamForPackingBytes(signal: List<List<Float>>): AudioInputStream {
        val channels = 1 // Always mono
        val isSigned = encoding == AudioFormatEncodings.PCM_SIGNED
        val format = AudioFormat(sampleRate.toFloat(), sampleSizeInBits, channels, isSigned, bigEndian)
        val inputStream = AudioBytesPacker(format, signal, this)
        return AudioInputStream(inputStream, format, signal[0].size.toLong())
    }
}

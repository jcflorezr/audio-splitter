package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.core.util.SupportedAudioFormats
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioFormatEncodings
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.AudioSegmentsRepository
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import net.jcflorezr.transcriber.audio.splitter.domain.util.AudioBytesPacker
import net.jcflorezr.transcriber.audio.splitter.domain.util.AudioBytesUnPacker
import net.jcflorezr.transcriber.core.domain.Command
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

interface AudioClipsFilesGenerator {
    suspend fun generateAudioClipFile(audioClipInfo: AudioClip)
}

class AudioClipsFilesGeneratorImpl(
    private val audioSegmentsRepository: AudioSegmentsRepository,
    private val sourceFileInfoRepository: SourceFileInfoRepository,
    private val tempLocalDirectory: String,
    private val command: Command
) : AudioClipsFilesGenerator {

    companion object {
        private val FLAC_TYPE = SupportedAudioFormats.FLAC.fileType
        private val FLAC_EXTENSION = SupportedAudioFormats.FLAC.extension
    }

    override suspend fun generateAudioClipFile(audioClipInfo: AudioClip) = withContext<Unit>(Dispatchers.IO) {
        val firstSegment = audioClipInfo.activeSegments.first()
        val lastSegment = audioClipInfo.activeSegments.last()
        val sourceAudioFileName = firstSegment.sourceAudioFileName
        val audioClipName = audioClipInfo.audioClipFileName()
        val audioContentInfo = sourceFileInfoRepository.findBy(firstSegment.sourceAudioFileName).audioContentInfo
        audioSegmentsRepository.findBy(
            sourceAudioFileName = sourceAudioFileName,
            segmentStartInSeconds = firstSegment.segmentStart,
            segmentEndInSeconds = lastSegment.segmentEnd)
        .map { it.audioSegmentBytes.bytes }
        .reduce { currentSegmentBytes, nextSegmentBytes -> currentSegmentBytes + nextSegmentBytes }
        .let { byteArray ->
            val audioClipSignal = AudioBytesUnPacker.generateAudioSignal(audioContentInfo, byteArray)
            val audioClipInputStream = getAudioInputStreamForPackingBytes(audioClipSignal, audioContentInfo)
            val pathToStoreClipFile = "$tempLocalDirectory/audio-clips/$sourceAudioFileName"
            File(pathToStoreClipFile).mkdirs()
            val audioClipFile = File("$pathToStoreClipFile/$audioClipName.$FLAC_EXTENSION")
            AudioSystem.write(audioClipInputStream, FLAC_TYPE, audioClipFile)
        }
        launch { command.execute(aggregateRoot = audioClipInfo) }
    }

    private fun getAudioInputStreamForPackingBytes(signal: List<List<Float>>, audioContentInfo: AudioContentInfo): AudioInputStream {
        val channels = 1
        val signed = audioContentInfo.encoding == AudioFormatEncodings.PCM_SIGNED
        val format =
            AudioFormat(
                audioContentInfo.sampleRate.toFloat(),
                audioContentInfo.sampleSizeInBits,
                channels,
                signed,
                audioContentInfo.bigEndian)
        val inputStream = AudioBytesPacker(format, signal, audioContentInfo)
        return AudioInputStream(inputStream, format, signal[0].size.toLong())
    }
}
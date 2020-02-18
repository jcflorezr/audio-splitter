package net.jcflorezr.signal

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.jcflorezr.broker.Topic
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.model.AudioSourceInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.util.AudioFormats
import net.jcflorezr.util.AudioUtils
import net.jcflorezr.util.PropsUtils
import java.io.File
import java.io.IOException
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

interface AudioIo {
    suspend fun saveAudioFile(fileName: String, extension: String, signal: Array<FloatArray?>, sampleRate: Int, transactionId: String): Boolean
    suspend fun generateAudioSignalFromAudioFile(configuration: InitialConfiguration)
}

class AudioIoImpl(
    private val propsUtils: PropsUtils,
    private val audioSignalTopic: Topic<AudioSignal>
) : AudioIo {

    private val logger = KotlinLogging.logger { }

    override suspend fun saveAudioFile(
        fileName: String,
        extension: String,
        signal: Array<FloatArray?>,
        sampleRate: Int,
        transactionId: String
    ): Boolean {
        logger.info { "[$transactionId][6][audio-clip] Creating Clip Audio File ($fileName$extension)." }
        val audioInputStream = getAudioInputStreamForPacking(signal, 0, signal[0]!!.size, sampleRate)
        val fileType = AudioFormats.getFileType(extension)
        return AudioSystem.write(audioInputStream, fileType, File("$fileName$extension")) > 0
    }

    override suspend fun generateAudioSignalFromAudioFile(configuration: InitialConfiguration) = coroutineScope<Unit> {
        val sourceAudioFileName = configuration.audioFileName
        AudioSystem.getAudioInputStream(File(configuration.convertedAudioFileLocation ?: sourceAudioFileName)).use { stream ->
            logger.info { "[${propsUtils.getTransactionId(sourceAudioFileName)}][2][audio-signal] " +
                "Starting to generate audio signal segments for $sourceAudioFileName." }

            val totalFrames = stream.frameLength.takeIf { it <= Integer.MAX_VALUE }?.toInt()
                ?: throw IOException("Sound file too long.")
            val sampleRate = stream.format.sampleRate.toInt()
            val blockFrames = sampleRate.takeIf { it < totalFrames } ?: totalFrames // used to be 0x4000
            val audioInfo = AudioSourceInfo.getAudioInfo(stream.format)
            val (position, index) = 0 to 1
            generateSequence(position to index) {
                val requiredFrames = Math.min(totalFrames - it.first, blockFrames).let {
                    reqFrames -> if (reqFrames < blockFrames) { reqFrames * audioInfo.frameSize } else { reqFrames }
                }
                val bytesBuffer = ByteArray(requiredFrames) // used to be byte[frameSize * blockFrames]
                val bytesRead = stream.read(bytesBuffer, 0, requiredFrames)
                when {
                    bytesRead <= 0 ->
                        throw IOException("Unexpected EOF while reading WAV file. totalFrames=" + requiredFrames + " pos=" + it + " frameSize=" + audioInfo.frameSize + ".")
                    bytesRead % audioInfo.frameSize != 0 ->
                        throw IOException("Length of transmitted signal is not a multiple of frame size. requiredFrames=" + requiredFrames + " trBytes=" + bytesRead + " frameSize=" + audioInfo.frameSize + ".")
                }
                val framesToRead = bytesRead / audioInfo.frameSize
                val audioSignal = AudioSignal(
                    audioFileName = configuration.audioFileMetadata!!.audioFileName,
                    index = it.first.toFloat() / sampleRate.toFloat(),
                    sampleRate = sampleRate,
                    totalFrames = totalFrames,
                    initialPosition = it.first,
                    initialPositionInSeconds = AudioUtils.tenthsSecondsFormat(it.first.toFloat() / sampleRate.toFloat()).toFloat(),
                    endPosition = it.first + framesToRead,
                    endPositionInSeconds = AudioUtils.tenthsSecondsFormat((it.first + framesToRead).toFloat() / sampleRate.toFloat()).toFloat(),
                    data = AudioBytesUnpacker.generateAudioSignal(audioInfo, bytesBuffer, framesToRead),
                    dataInBytes = bytesBuffer,
                    audioSourceInfo = audioInfo
                )
                launch {
                    logger.info { "[${propsUtils.getTransactionId(sourceAudioFileName)}][2][audio-signal] " +
                        "Audio signal ==> start: ${audioSignal.initialPositionInSeconds} - " +
                        "end: ${audioSignal.endPositionInSeconds}. has been generated." }
                    audioSignalTopic.postMessage(message = audioSignal)
                }
                Pair(it.first + framesToRead, it.second + 1)
            }.takeWhile { it.first < totalFrames }
            .count()
            configuration.convertedAudioFileLocation?.let { File(it).delete() }
        }
    }

    private fun getAudioInputStreamForPacking(signal: Array<FloatArray?>, pos: Int, len: Int, sampleRate: Int): AudioInputStream {
        val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        val audioBytesPacker = AudioBytesPacker(format, signal, pos, len)
        return AudioInputStream(audioBytesPacker, format, len.toLong())
    }
}
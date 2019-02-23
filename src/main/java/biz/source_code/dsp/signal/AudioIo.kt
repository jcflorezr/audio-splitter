package biz.source_code.dsp.sound

import biz.source_code.dsp.model.AudioFileWritingResult
import biz.source_code.dsp.model.AudioSignalKt
import biz.source_code.dsp.util.AudioFormatsSupported
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.jcflorezr.broker.Topic
import net.jcflorezr.model.AudioSourceInfo
import net.jcflorezr.model.InitialConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

interface AudioIo {
    suspend fun generateAudioSignalFromAudioFile(configuration: InitialConfiguration)
    fun saveAudioFile(fileName: String, extension: String, signal: Array<FloatArray?>, sampleRate: Int): AudioFileWritingResult
}

@Service
final class AudioIoImpl : AudioIo {

    @Autowired
    private lateinit var audioSignalTopic: Topic<AudioSignalKt>

    override fun saveAudioFile(fileName: String, extension: String, signal: Array<FloatArray?>, sampleRate: Int): AudioFileWritingResult {
        val audioInputStream = getAudioInputStreamForPacking(signal, 0, signal.size, sampleRate)
        val fileType = AudioFormatsSupported.getFileType(extension)
        return writeAudioFile(audioInputStream, fileType, fileName + extension)
    }

    override suspend fun generateAudioSignalFromAudioFile(configuration: InitialConfiguration) = coroutineScope<Unit> {
        AudioSystem.getAudioInputStream(File(configuration.convertedAudioFileLocation)).use { stream ->
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
                        throw IOException("Length of transmitted data is not a multiple of frame size. requiredFrames=" + requiredFrames + " trBytes=" + bytesRead + " frameSize=" + audioInfo.frameSize + ".")
                }
                val framesToRead = bytesRead / audioInfo.frameSize
                val audioSignal = AudioSignalKt(
                    audioFileName = configuration.audioFileMetadata!!.audioFileName,
                    index = it.first.toFloat() / sampleRate.toFloat(),
                    sampleRate = sampleRate,
                    totalFrames = totalFrames,
                    initialPosition = it.first,
                    initialPositionInSeconds = it.first.toFloat() / sampleRate.toFloat(),
                    endPosition = it.first + framesToRead,
                    endPositionInSeconds = (it.first + framesToRead).toFloat() / sampleRate.toFloat(),
                    data = AudioBytesUnpacker.generateAudioSignal(audioInfo, bytesBuffer, framesToRead),
                    dataInBytes = bytesBuffer,
                    audioSourceInfo = audioInfo
                )
                launch {
                    audioSignalTopic.postMessage(message = audioSignal)
                }
                Pair(it.first + framesToRead, it.second + 1)
            }.takeWhile { it.first < totalFrames }
            .toList()
            // TODO: return an object for auditing process, perhaps an Aspect receive the consolidated result
        }
    }

    private fun getAudioInputStreamForPacking(signal: Array<FloatArray?>, pos: Int, len: Int, sampleRate: Int): AudioInputStream {
        val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
        val audioBytesPacker = AudioBytesPacker(format, signal, pos, len)
        return AudioInputStream(audioBytesPacker, format, len.toLong())
    }

    private fun writeAudioFile(audioInputStream: AudioInputStream, fileType: AudioFileFormat.Type, fileName: String): AudioFileWritingResult {
        return try {
            AudioSystem.write(audioInputStream, fileType, File(fileName))
            AudioFileWritingResult.successful()
        } catch (e: Exception) {
            AudioFileWritingResult.unsuccessful(e)
        }
    }

}
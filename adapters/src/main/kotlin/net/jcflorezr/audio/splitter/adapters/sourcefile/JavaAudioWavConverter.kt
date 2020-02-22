package net.jcflorezr.audio.splitter.adapters.sourcefile

import mu.KotlinLogging
import net.jcflorezr.audio.splitter.adapters.util.SupportedAudioFormats
import net.jcflorezr.audio.splitter.domain.sourcefile.AudioWavConverter
import org.apache.tika.Tika
import java.io.File
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED
import javax.sound.sampled.AudioSystem

class JavaAudioWavConverter(
    private val tempLocalDirectory: String
) : AudioWavConverter {

    private val logger = KotlinLogging.logger { }
    private val sampleSizeInBits = 16

    override fun createAudioWavFile(originalAudioFile: File): File? =
        SupportedAudioFormats.getExtension(Tika().detect(originalAudioFile.absolutePath))
            .takeIf { it !=  SupportedAudioFormats.WAV && it != SupportedAudioFormats.WAVE }
            ?.let { createAudioWavFile(originalAudioFile.nameWithoutExtension, originalAudioFile.absolutePath) }

    private fun createAudioWavFile(audioFileName: String, audioFileLocation: String): File {
        logger.info { "[1][entry-point] Converting source audio file ($audioFileName) to ${SupportedAudioFormats.WAV}" }
        val file = File(audioFileLocation)
        val audioSourceInputStream = AudioSystem.getAudioInputStream(file)
        val baseFormat = audioSourceInputStream.format
        val decodedFormat = AudioFormat(
            PCM_SIGNED, baseFormat.sampleRate, sampleSizeInBits, baseFormat.channels, baseFormat.channels * 2, baseFormat.sampleRate, false)
        val audioWavInputStream = AudioSystem.getAudioInputStream(decodedFormat, audioSourceInputStream)
        val audioWavFile = File("$tempLocalDirectory/$audioFileName.${SupportedAudioFormats.WAV.extension}")
        AudioSystem.write(audioWavInputStream, AudioFileFormat.Type.WAVE, audioWavFile)
        return audioWavFile
    }
}
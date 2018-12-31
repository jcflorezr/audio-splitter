package net.jcflorezr.core

import javazoom.jl.decoder.JavaLayerException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.jcflorezr.broker.MessageLauncher
import net.jcflorezr.broker.Topic
import net.jcflorezr.exception.AudioFileLocationException
import net.jcflorezr.exception.InternalServerErrorException
import net.jcflorezr.model.AudioFileMetadata
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.util.AudioFormats
import net.jcflorezr.util.AudioUtilsKt
import net.jcflorezr.util.AudioUtilsKt.javaZoomAudioConverter
import net.jcflorezr.util.AudioUtilsKt.tikaAudioParser
import net.jcflorezr.util.JsonUtilsKt
import org.apache.commons.io.FilenameUtils
import org.apache.tika.exception.TikaException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.xml.sax.SAXException
import java.io.File
import java.io.FileInputStream
import java.io.IOException

interface AudioSplitter {
    fun generateAudioClips(configuration: InitialConfiguration)
}

@Service
final class AudioSplitterImpl : AudioSplitter {

    @Autowired
    private lateinit var messageLauncher: MessageLauncher<InitialConfiguration>

    override fun generateAudioClips(configuration: InitialConfiguration) {
        val sourceAudioFile = configuration.validateConfiguration()
        val convertedAudioFile = sourceAudioFile.convertFileToWavIfNeeded()
        val initialConfiguration = configuration.copy(
            convertedAudioFileLocation = convertedAudioFile.name,
            audioFileMetadata = sourceAudioFile.extractAudioMetadata()
        )
        messageLauncher.launchMessage(msg = initialConfiguration)
        // TODO: Should return a Response object
    }

    private fun InitialConfiguration.validateConfiguration(): File {
        val sourceAudioFile = audioFileLocation
            .takeIf { it.isNotBlank() }
            ?.let { File(it) } ?: throw AudioFileLocationException.mandatoryFieldsMissingException()
        when {
            !sourceAudioFile.exists() -> throw AudioFileLocationException.audioFileDoesNotExist(sourceAudioFile.name)
            sourceAudioFile.isDirectory -> throw AudioFileLocationException.audioFileShouldNotBeDirectory(sourceAudioFile.name)
        }
        return sourceAudioFile
    }

    private fun File.convertFileToWavIfNeeded() : File {
        val audioFileName = absolutePath
        val audioFormat = AudioFormats.getExtension(tikaAudioParser.detect(audioFileName))
        return when(audioFormat) {
            AudioFormats.WAV, AudioFormats.WAVE -> this
            else -> convertAudioFile(audioFileName)
        }
    }

    private fun convertAudioFile(audioFileLocation: String): File {
        try {
            val newAudioFileLocation = FilenameUtils.getBaseName(audioFileLocation) + AudioFormats.WAV.extension
            javaZoomAudioConverter.convert(audioFileLocation, newAudioFileLocation)
            return File(newAudioFileLocation)
        } catch (e: JavaLayerException) {
            throw InternalServerErrorException(e)
        }
    }

    @Throws(TikaException::class, SAXException::class, IOException::class)
    private fun File.extractAudioMetadata(): AudioFileMetadata {
        FileInputStream(this).use { inputStream ->
            val (metadata, bodyContentHandler) = AudioUtilsKt.parse(inputStream)
            return metadata.names()
                .associate { it to metadata.get(it) }.toMutableMap<String, Any>()
                .let {
                    it["audioFileName"] = this.name
                    it["rawMetadata"] = bodyContentHandler.toString().split("\n").ifEmpty { ArrayList() }
                    JsonUtilsKt.convertMapToPojo(map = it, pojoClass = AudioFileMetadata::class.java)
                }
        }
    }

}
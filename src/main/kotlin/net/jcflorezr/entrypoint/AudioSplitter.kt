package net.jcflorezr.entrypoint

import javazoom.jl.decoder.JavaLayerException
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
import org.apache.tika.exception.TikaException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.xml.sax.SAXException
import java.io.File
import java.io.FileInputStream
import java.io.IOException

interface AudioSplitter {
    suspend fun splitAudioIntoClips(configuration: InitialConfiguration)
}

@Service
final class AudioSplitterImpl : AudioSplitter {

    @Autowired
    private lateinit var messageLauncher: Topic<InitialConfiguration>

    private val tempConvertedFilesPath: String
    private val thisClass: Class<AudioSplitterImpl> = this.javaClass

    init {
        tempConvertedFilesPath = thisClass.getResource("/temp-converted-files").path
    }

    override suspend fun splitAudioIntoClips(configuration: InitialConfiguration) {
        val sourceAudioFile = configuration.validateConfiguration()
        val convertedAudioFile = sourceAudioFile.convertFileToWavIfNeeded()
        val initialConfiguration = configuration.copy(
            convertedAudioFileLocation = convertedAudioFile?.name,
            audioFileMetadata = sourceAudioFile.extractAudioMetadata()
        )
        messageLauncher.postMessage(message = initialConfiguration)
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

    private fun File.convertFileToWavIfNeeded() : File? {
        val audioFileName = nameWithoutExtension
        val audioFileLocation = absolutePath
        val audioFormat = AudioFormats.getExtension(tikaAudioParser.detect(audioFileLocation))
        return when(audioFormat) {
            AudioFormats.WAV, AudioFormats.WAVE -> null
            else -> convertAudioFile(audioFileName, audioFileLocation)
        }
    }

    private fun convertAudioFile(audioFileName: String, audioFileLocation: String): File {
        try {
            val tempConvertedAudioFile = "$tempConvertedFilesPath/$audioFileName${AudioFormats.WAV.extension}"
            javaZoomAudioConverter.convert(audioFileLocation, tempConvertedAudioFile)
            return File(tempConvertedAudioFile)
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
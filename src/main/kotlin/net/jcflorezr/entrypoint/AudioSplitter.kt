package net.jcflorezr.entrypoint

import javazoom.jl.decoder.JavaLayerException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.jcflorezr.broker.Topic
import net.jcflorezr.exception.SourceAudioFileValidationException
import net.jcflorezr.exception.InternalServerErrorException
import net.jcflorezr.model.AudioFileMetadata
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.util.AudioFormats
import net.jcflorezr.util.AudioUtils
import net.jcflorezr.util.AudioUtils.javaZoomAudioConverter
import net.jcflorezr.util.AudioUtils.tikaAudioParser
import net.jcflorezr.util.JsonUtils
import net.jcflorezr.util.PropsUtils
import org.apache.tika.exception.TikaException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.xml.sax.SAXException
import java.io.File
import java.io.FileInputStream
import java.io.IOException

interface AudioSplitter {
    fun splitAudioIntoClips(configuration: InitialConfiguration)
}

@Service
final class AudioSplitterImpl : AudioSplitter {

    @Autowired
    private lateinit var messageLauncher: Topic<InitialConfiguration>

    private val logger = KotlinLogging.logger { }
    private val tempConvertedFilesPath: String
    private val thisClass: Class<AudioSplitterImpl> = this.javaClass

    init {
        tempConvertedFilesPath = thisClass.getResource("/temp-converted-files").path
    }

    // TODO: this method should be marked as rest api endpoint
    override fun splitAudioIntoClips(configuration: InitialConfiguration) = runBlocking<Unit> {
        logger.info { "[1][entry-point] Audio Splitter process for audio file: ${configuration.audioFileLocation} has just started" }
        val sourceAudioFile = configuration.validateConfiguration()
        val convertedAudioFile = sourceAudioFile.convertFileToWavIfNeeded()
        PropsUtils.setTransactionIdProperty(sourceAudioFile)
        PropsUtils.setDirectoryPathProperty(outputDirectory = configuration.outputDirectory)
        val initialConfiguration = configuration.copy(
            convertedAudioFileLocation = convertedAudioFile?.absolutePath,
            audioFileMetadata = sourceAudioFile.extractAudioMetadata()
        )
        launch {
            messageLauncher.postMessage(message = initialConfiguration)
        }
        // TODO: Should return a Response object
    }

    private fun InitialConfiguration.validateConfiguration(): File {
        logger.info { "[1][entry-point] Validating received configuration. Source audio file location: $audioFileLocation; " +
            "Output directory: $outputDirectory" }
        val sourceAudioFile = audioFileLocation
            .takeIf { it.isNotBlank() }
            ?.let { File(it) } ?: throw SourceAudioFileValidationException.mandatoryFieldsMissingException()
        when {
            !sourceAudioFile.exists() -> throw SourceAudioFileValidationException.audioFileDoesNotExist(sourceAudioFile.absolutePath)
            sourceAudioFile.isDirectory -> throw SourceAudioFileValidationException.audioFileShouldNotBeDirectory(sourceAudioFile.absolutePath)
        }
        outputDirectory.takeIf { it.isNotBlank() }
            ?.takeIf { File(it).isDirectory } ?: throw SourceAudioFileValidationException.invalidOutputDirectoryPath(outputDirectory)
        return sourceAudioFile
    }

    private fun File.convertFileToWavIfNeeded() : File? {
        logger.info { "[1][entry-point] Checking if source audio file ($name) must be converted to ${AudioFormats.WAV}"}
        val audioFileName = nameWithoutExtension
        val audioFileLocation = absolutePath
        val audioFormat = AudioFormats.getExtension(tikaAudioParser.detect(audioFileLocation))
        return when(audioFormat) {
            AudioFormats.WAV, AudioFormats.WAVE -> null
            else -> convertAudioFile(audioFileName, audioFileLocation)
        }
    }

    private fun convertAudioFile(audioFileName: String, audioFileLocation: String): File {
        logger.info { "[1][entry-point] Converting source audio file ($audioFileName) to ${AudioFormats.WAV}"}
        try {
            val tempConvertedAudioFile = "$tempConvertedFilesPath/$audioFileName${AudioFormats.WAV.extension}"
            javaZoomAudioConverter.convert(audioFileLocation, tempConvertedAudioFile)
            return File(tempConvertedAudioFile)
        } catch (e: JavaLayerException) {
            throw InternalServerErrorException(ex = e)
        }
    }

    @Throws(TikaException::class, SAXException::class, IOException::class)
    private fun File.extractAudioMetadata(): AudioFileMetadata {
        FileInputStream(this).use { inputStream ->
            val (metadata, bodyContentHandler) = AudioUtils.parse(inputStream)
            logger.info { "[${PropsUtils.getTransactionId(name)}][1][entry-point] Extracting metadata for audio file: $name." }
            return metadata.names()
                .associate { it to metadata.get(it) }.toMutableMap<String, Any>()
                .let {
                    it["audioFileName"] = this.name
                    it["rawMetadata"] = bodyContentHandler.toString().split("\n").ifEmpty { ArrayList() }
                    JsonUtils.convertMapToPojo(map = it, pojoClass = AudioFileMetadata::class.java)
                }
        }
    }

}
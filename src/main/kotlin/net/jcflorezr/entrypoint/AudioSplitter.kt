package net.jcflorezr.entrypoint

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.jcflorezr.broker.Topic
import net.jcflorezr.exception.AudioSplitterException
import net.jcflorezr.exception.ExceptionHandler
import net.jcflorezr.exception.InternalServerErrorException
import net.jcflorezr.exception.SourceAudioFileValidationException
import net.jcflorezr.model.AudioFileMetadata
import net.jcflorezr.model.AudioSplitterResponse
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.model.SuccessResponse
import net.jcflorezr.storage.BucketClient
import net.jcflorezr.util.AudioFormats
import net.jcflorezr.util.AudioUtils
import net.jcflorezr.util.PropsUtils
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.io.File
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem

interface AudioSplitter {
    fun splitAudioIntoClips(configuration: InitialConfiguration): AudioSplitterResponse
}

@RestController
@RequestMapping("/v1/audio-splitter")
final class AudioSplitterImpl(
    private val propsUtils: PropsUtils,
    private val messageLauncher: Topic<InitialConfiguration>,
    private val bucketClient: BucketClient,
    private val exceptionHandler: ExceptionHandler
) : AudioSplitter {

    private val logger = KotlinLogging.logger { }
    private val tempConvertedFilesPath: String
    private val thisClass: Class<AudioSplitterImpl> = this.javaClass

    init {
        tempConvertedFilesPath = thisClass.getResource("/temp-converted-files").path
    }

    @PostMapping(
        value = ["/split-audio-into-clips"],
        consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
        produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
    )
    @ResponseBody
    override fun splitAudioIntoClips(@RequestBody configuration: InitialConfiguration) = runBlocking {
        logger.info { "[1][entry-point] Audio Splitter local process for audio file: " +
            "${configuration.audioFileName} has just started" }
        try {
            val sourceAudioFile = configuration.validateConfiguration()
            val convertedAudioFile = sourceAudioFile.convertFileToWavIfNeeded()
            propsUtils.setTransactionId(sourceAudioFile)
            propsUtils.setSourceFileLocation(sourceAudioFile, propsUtils.getTransactionId(sourceAudioFile.name))
            val initialConfiguration = configuration.copy(
                audioFileName = sourceAudioFile.absolutePath,
                convertedAudioFileLocation = convertedAudioFile?.absolutePath,
                audioFileMetadata = sourceAudioFile.extractAudioMetadata()
            )
            launch {
                messageLauncher.postMessage(message = initialConfiguration)
            }
            SuccessResponse(
                message = "The audio file '${configuration.audioFileName}' is being splitted. " +
                    "You will be notified when the split process is done.",
                transactionId = propsUtils.getTransactionId(configuration.audioFileName)
            )
        } catch (audioSplitterException: AudioSplitterException) {
            throw audioSplitterException
        } catch (ex: Exception) {
            exceptionHandler.handle(ex, configuration.audioFileName)
            throw InternalServerErrorException(ex = ex)
        }
    }

    private fun InitialConfiguration.validateConfiguration(): File {
        logger.info { "[1][entry-point] Validating received local configuration. Source audio file: $audioFileName;" }
        if (audioFileName.isBlank()) {
            throw SourceAudioFileValidationException.mandatoryFieldsMissingException()
        }
        val sourceAudioFile = bucketClient.downloadSourceFileFromBucket(audioFileName)
        when {
            !sourceAudioFile.exists() -> throw SourceAudioFileValidationException.audioFileDoesNotExist(sourceAudioFile.absolutePath)
            sourceAudioFile.isDirectory -> throw SourceAudioFileValidationException.audioFileShouldNotBeDirectory(sourceAudioFile.absolutePath)
        }
        return sourceAudioFile
    }

    private fun File.convertFileToWavIfNeeded(): File? {
        logger.info { "[1][entry-point] Checking if source audio file ($name) must be converted to ${AudioFormats.WAV}" }
        val audioFileName = nameWithoutExtension
        val audioFileLocation = absolutePath
        val audioFormat = AudioFormats.getExtension(AudioUtils.tikaAudioParser.detect(audioFileLocation))
        return when (audioFormat) {
            AudioFormats.WAV, AudioFormats.WAVE -> null
            else -> convertAudioFile(audioFileName, audioFileLocation)
        }
    }

    private fun convertAudioFile(audioFileName: String, audioFileLocation: String): File {
        logger.info { "[1][entry-point] Converting source audio file ($audioFileName) to ${AudioFormats.WAV}" }
        val file = File(audioFileLocation)
        val audioSourceInStream = AudioSystem.getAudioInputStream(file)
        val baseFormat = audioSourceInStream.format
        val decodedFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            baseFormat.sampleRate,
            16,
            baseFormat.channels,
            baseFormat.channels * 2,
            baseFormat.sampleRate,
            false
        )
        val audioDestInStream = AudioSystem.getAudioInputStream(decodedFormat, audioSourceInStream)
        val tempConvertedAudioFile = File("$tempConvertedFilesPath/$audioFileName${AudioFormats.WAV.extension}")
        AudioSystem.write(audioDestInStream, AudioFileFormat.Type.WAVE, tempConvertedAudioFile)
        return tempConvertedAudioFile
    }

    private fun File.extractAudioMetadata() = AudioFileMetadata.getAudioFileMetadata(this)
}
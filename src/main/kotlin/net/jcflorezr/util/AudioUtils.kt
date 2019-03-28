package net.jcflorezr.util

import com.fasterxml.uuid.Generators
import javazoom.jl.converter.Converter
import net.jcflorezr.exception.SourceAudioFileValidationException
import org.apache.commons.io.FilenameUtils
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import java.io.File
import java.io.InputStream

object AudioUtils {

    val tikaAudioParser = Tika()
    private val tikaDetectParser = AutoDetectParser()
    val javaZoomAudioConverter = Converter()

    private const val oneDigitDecimalFormat = "%.1f"
    private const val threeDigitDecimalFormat = "%.3f"

    fun tenthsSecondsFormat(value: Float) = tenthsSecondsFormat(value.toDouble())

    fun tenthsSecondsFormat(value: Double) = oneDigitDecimalFormat.format(value).toDouble()

    fun millisecondsFormat(value: Float) = millisecondsFormat(value.toDouble()).toFloat()

    fun millisecondsFormat(value: Double) = threeDigitDecimalFormat.format(value).toDouble()

    fun parse(inputStream: InputStream): Pair<Metadata, BodyContentHandler> {
        val metadata = Metadata()
        val bodyContentHandler = BodyContentHandler()
        tikaDetectParser.parse(inputStream, bodyContentHandler, metadata, ParseContext())
        return metadata to bodyContentHandler
    }

}

object PropsUtils {

    private val sourceFileNames = HashSet<String>()
    private const val outputDirectoryPropertyName = "output-directory"
    private const val transactionIdPropertyName = "transaction-id"

    fun setTransactionIdProperty(sourceAudioFile: File) {
        val sourceAudioFileBaseName = sourceAudioFile.nameWithoutExtension
        if (sourceFileNames.contains(sourceAudioFileBaseName)) {
            throw SourceAudioFileValidationException.existingAudioSplitProcessException(sourceAudioFileBaseName)
        }
        val uuid = Generators.timeBasedGenerator().generate()
        System.setProperty(
            "${transactionIdPropertyName}_$sourceAudioFileBaseName",
            "${uuid}_$sourceAudioFileBaseName"
        )
    }

    fun setDirectoryPathProperty(outputDirectory: String) {
        System.setProperty(outputDirectoryPropertyName, outputDirectory)
    }

    fun getTransactionId(sourceAudioFileName: String): String {
        val sourceAudioFileBaseName = File(sourceAudioFileName).nameWithoutExtension
        return System.getProperty("${transactionIdPropertyName}_$sourceAudioFileBaseName")
    }

    fun getDirectoryPath(): String = System.getProperty(outputDirectoryPropertyName)

}

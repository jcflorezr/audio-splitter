package net.jcflorezr.util

import com.fasterxml.uuid.Generators
import net.jcflorezr.exception.SourceAudioFileValidationException
import org.apache.tika.Tika
import org.springframework.stereotype.Service
import java.io.File

object AudioUtils {

    val tikaAudioParser = Tika()

    private const val oneDigitDecimalFormat = "%.1f"
    private const val threeDigitDecimalFormat = "%.3f"

    fun tenthsSecondsFormat(value: Float) = tenthsSecondsFormat(value.toDouble())

    fun tenthsSecondsFormat(value: Double) = oneDigitDecimalFormat.format(value).toDouble()

    fun millisecondsFormat(value: Float) = millisecondsFormat(value.toDouble()).toFloat()

    fun millisecondsFormat(value: Double) = threeDigitDecimalFormat.format(value).toDouble()
}

@Service
final class PropsUtils {

    private val sourceFileNames = HashSet<String>()

    companion object {
        private const val transactionIdPropertyName = "transaction-id"
    }

    fun setTransactionId(sourceAudioFile: File) {
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

    fun getTransactionId(sourceAudioFileName: String): String {
        val sourceAudioFileBaseName = File(sourceAudioFileName).nameWithoutExtension
        return System.getProperty("${transactionIdPropertyName}_$sourceAudioFileBaseName")
    }

    fun setSourceFileLocation(sourceAudioFile: File, transactionId: String) {
        System.setProperty(transactionId, sourceAudioFile.absolutePath)
    }

    fun getSourceFileLocation(transactionId: String) = File(System.getProperty(transactionId))
}
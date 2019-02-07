package net.jcflorezr.util

import javazoom.jl.converter.Converter
import org.apache.tika.Tika
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import java.io.InputStream

// TODO: rename it to AudioUtils
object AudioUtilsKt {

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


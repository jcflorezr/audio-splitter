package net.jcflorezr.transcriber.core.util

object FloatingPointUtils {

    private const val oneDigitDecimalFormat = "%.1f"
    private const val threeDigitDecimalFormat = "%.3f"

    fun tenthsSecondsFormat(value: Float) = tenthsSecondsFormat(value.toDouble())

    fun tenthsSecondsFormat(value: Double) = oneDigitDecimalFormat.format(value).toDouble()

    fun millisecondsFormat(value: Float) = millisecondsFormat(value.toDouble()).toFloat()

    fun millisecondsFormat(value: Double) = threeDigitDecimalFormat.format(value).toDouble()
}

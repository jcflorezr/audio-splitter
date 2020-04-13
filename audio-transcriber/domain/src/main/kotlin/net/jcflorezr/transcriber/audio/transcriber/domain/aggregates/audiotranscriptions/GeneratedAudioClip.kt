package net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions

import net.jcflorezr.transcriber.core.exception.FileException
import java.io.File

data class GeneratedAudioClip(
    val sourceAudioFileName: String,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int,
    val audioClipFileName: String,
    val audioClipFile: File
) {

    companion object {

        fun createNew(
            sourceAudioFileName: String,
            hours: Int,
            minutes: Int,
            seconds: Int,
            tenthsOfSecond: Int,
            audioClipFileName: String,
            audioClipFile: File
        ) = GeneratedAudioClip(
            sourceAudioFileName = sourceAudioFileName,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            tenthsOfSecond = tenthsOfSecond,
            audioClipFileName = audioClipFileName,
            audioClipFile = audioClipFile
                .takeIf { it.exists() }
                ?: throw FileException.fileNotFound(audioClipFile.absolutePath))
    }
}
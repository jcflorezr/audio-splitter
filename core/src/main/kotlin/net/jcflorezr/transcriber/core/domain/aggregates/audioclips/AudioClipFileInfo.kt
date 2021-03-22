package net.jcflorezr.transcriber.core.domain.aggregates.audioclips

import net.jcflorezr.transcriber.core.domain.AggregateRoot

/*
    Aggregate Root
 */
data class AudioClipFileInfo(
    val sourceAudioFileName: String,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val tenthsOfSecond: Int,
    val audioClipFileName: String,
    val audioClipFileExtension: String
) : AggregateRoot {

    companion object {
        fun createNew(
            sourceAudioFileName: String,
            hours: Int,
            minutes: Int,
            seconds: Int,
            tenthsOfSecond: Int,
            audioClipFileName: String,
            audioClipFileExtension: String
        ) = AudioClipFileInfo(
            sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond, audioClipFileName, audioClipFileExtension
        )
    }
}

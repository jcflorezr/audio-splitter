package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audioclips

import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipFileGeneratedDummyHandler
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventDispatcherDI
import net.jcflorezr.transcriber.audio.splitter.domain.commands.audioclip.GenerateAudioClipFile

@ObsoleteCoroutinesApi
object GenerateAudioClipFileCommandDI {

    val audioClipFileCommand =
        GenerateAudioClipFile(AudioSplitterKafkaEventDispatcherDI.audioSplitterTestKafkaDispatcher)

    val tempLocalDirectory: String = this.javaClass.getResource("/temp-converted-files/audio-clips").path

    // Event Handler
    val dummyEventHandler = AudioClipFileGeneratedDummyHandler(tempLocalDirectory)
}

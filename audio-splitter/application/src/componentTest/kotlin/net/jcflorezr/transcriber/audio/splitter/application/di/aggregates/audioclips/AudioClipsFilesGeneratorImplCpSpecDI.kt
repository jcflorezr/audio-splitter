package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audioclips

import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.audiosegments.DefaultAudioSegmentsRepository
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.sourcefileinfo.DefaultSourceFileInfoRepository
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipFileGeneratedDummyHandler
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipsFilesGeneratorImpl
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventConsumerDI
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventDispatcherDI
import net.jcflorezr.transcriber.audio.splitter.domain.commands.audioclip.GenerateAudioClipFile
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioClipsFilesGenerator
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.audiosegments.AudioSegmentsRepository
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import org.mockito.Mockito.mock

/*
    Service
 */

@ObsoleteCoroutinesApi
object AudioClipsFilesGeneratorImplCpSpecDI : CoroutineVerticle() {

    private val audioSegmentsRepositoryMock = mock(DefaultAudioSegmentsRepository::class.java)
    private val sourceFileInfoRepositoryMock = mock(DefaultSourceFileInfoRepository::class.java)

    val audioClipsFilesGenerator: AudioClipsFilesGenerator =
        AudioClipsFilesGeneratorImpl(
            audioSegmentsRepositoryMock(),
            sourceFileInfoRepositoryMock(),
            GenerateAudioClipFileCommandDI.tempLocalDirectory,
            GenerateAudioClipFileCommandDI.audioClipFileCommand
        )

    override suspend fun start() {
        vertx.deployVerticle(AudioSplitterKafkaEventConsumerDI)
    }

    fun audioClipFileGeneratedEventHandler() = GenerateAudioClipFileCommandDI.dummyEventHandler
    fun audioSegmentsRepositoryMock(): AudioSegmentsRepository = audioSegmentsRepositoryMock
    fun sourceFileInfoRepositoryMock(): SourceFileInfoRepository = sourceFileInfoRepositoryMock
}

/*
    Command
 */

@ObsoleteCoroutinesApi
object GenerateAudioClipFileCommandDI {

    val audioClipFileCommand =
        GenerateAudioClipFile(AudioSplitterKafkaEventDispatcherDI.audioSplitterTestKafkaDispatcher)

    val tempLocalDirectory: String = this.javaClass.getResource("/temp-converted-files/audio-clips").path

    // Event Handler
    val dummyEventHandler = AudioClipFileGeneratedDummyHandler(tempLocalDirectory)
}

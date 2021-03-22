package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audioclips

import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips.AudioClipsInfoServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventConsumerDI
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioClipsInfoService

@ObsoleteCoroutinesApi
object AudioClipsInfoServiceImplCpSpecDI : CoroutineVerticle() {

    val audioClipsServiceImpl: AudioClipsInfoService =
        GenerateAudioClipInfoCommandDI.run { AudioClipsInfoServiceImpl(sourceFileInfoRepository, audioClipInfoCommand) }

    override suspend fun start() {
        vertx.deployVerticle(AudioSplitterKafkaEventConsumerDI)
    }

    fun audioClipInfoGeneratedEventHandler() = GenerateAudioClipInfoCommandDI.dummyEventHandler

    fun sourceFileInfoRepositoryMock() = GenerateAudioClipInfoCommandDI.sourceFileInfoRepository
}

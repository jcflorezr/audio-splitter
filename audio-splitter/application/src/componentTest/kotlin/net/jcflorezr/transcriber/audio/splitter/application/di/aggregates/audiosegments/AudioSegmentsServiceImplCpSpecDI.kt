package net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audiosegments

import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments.AudioSegmentsServiceImpl
import net.jcflorezr.transcriber.audio.splitter.application.di.events.AudioSplitterKafkaEventConsumerDI
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audiosegments.application.AudioSegmentsService

@ObsoleteCoroutinesApi
object AudioSegmentsServiceImplCpSpecDI : CoroutineVerticle() {

    val audioSegmentsServiceImpl: AudioSegmentsService =
        AudioSegmentsServiceImpl(GenerateAudioSegmentsCommandDI.audioFrameProcessor)

    override suspend fun start() {
        vertx.deployVerticle(AudioSplitterKafkaEventConsumerDI)
    }

    fun audioSegmentsGeneratedEventHandler() = GenerateAudioSegmentsCommandDI.dummyEventHandler

    fun sourceFileInfoRepositoryMock() = GenerateAudioSegmentsCommandDI.sourceFileInfoRepository
}

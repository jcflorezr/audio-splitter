package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audioclips.AudioClipsServiceImplCpSpecDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.core.util.JsonUtils.fromJsonToList
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when` as When

@ObsoleteCoroutinesApi
@ExtendWith(VertxExtension::class)
internal class AudioClipsInfoServiceImplCpSpec {

    private val mapper = ObjectMapper().registerKotlinModule()
    private val segmentsSourceFilesPath = this.javaClass.getResource("/audio-segments").path

    private val audioClipsServiceImplCpSpecDI = AudioClipsServiceImplCpSpecDI
    private val eventHandler = audioClipsServiceImplCpSpecDI.audioClipInfoGeneratedEventHandler()
    private val audioClipsInfoService = audioClipsServiceImplCpSpecDI.audioClipsServiceImpl
    private val sourceFileInfoRepository = audioClipsServiceImplCpSpecDI.sourceFileInfoRepositoryMock()

    @BeforeAll
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(AudioClipsServiceImplCpSpecDI, testContext.completing())
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
        testContext.completeNow()
    }

    @Test
    fun `generate audio clips for 'background noise low volume' segments`(testContext: VertxTestContext) = runBlocking {
        generateAudioClips(sourceAudioFileName = "background-noise-low-volume", testContext = testContext)
    }

    @Test
    fun `generate audio clips for 'strong background noise' segments`(testContext: VertxTestContext) = runBlocking {
        generateAudioClips(sourceAudioFileName = "strong-background-noise", testContext = testContext)
    }

    @Test
    fun `generate audio clips for 'with applause' segments`(testContext: VertxTestContext) = runBlocking {
        generateAudioClips(sourceAudioFileName = "with-applause", testContext = testContext)
    }

    private suspend fun generateAudioClips(sourceAudioFileName: String, testContext: VertxTestContext) = withContext(Dispatchers.IO) {
        val audioSourceFileInfoPath = "$segmentsSourceFilesPath/$sourceAudioFileName-file-info.json"
        val audioSourceFileInfo = mapper.readValue(File(audioSourceFileInfoPath), AudioSourceFileInfo::class.java)

        When(sourceFileInfoRepository.findBy(sourceAudioFileName)).thenReturn(audioSourceFileInfo)

        val audioSegments =
            fromJsonToList<AudioSegment>(jsonFile = File("$segmentsSourceFilesPath/$sourceAudioFileName-audio-segments.json"))
                .map { BasicAudioSegment.fromAudioSegment(it) }

        audioClipsInfoService.generateActiveSegments(audioSegments)

        eventHandler.assertAudioClips(sourceAudioFileName, testContext)
    }
}

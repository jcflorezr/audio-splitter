package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments

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
import net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audiosegments.AudioSegmentsServiceImplCpSpecDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when` as When

@ObsoleteCoroutinesApi
@ExtendWith(VertxExtension::class)
internal class AudioSegmentsServiceImplCpSpec {

    private val mapper = ObjectMapper().registerKotlinModule()
    private val audioSegmentsPath = this.javaClass.getResource("/audio-segments").path

    private val audioSegmentsServiceImplCpSpecDI = AudioSegmentsServiceImplCpSpecDI
    private val eventHandler = audioSegmentsServiceImplCpSpecDI.audioSegmentsGeneratedEventHandler()
    private val audioSegmentsService = AudioSegmentsServiceImplCpSpecDI.audioSegmentsServiceImpl
    private val sourceFileInfoRepository = AudioSegmentsServiceImplCpSpecDI.sourceFileInfoRepositoryMock()

    @BeforeAll
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(AudioSegmentsServiceImplCpSpecDI, testContext.completing())
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
        testContext.completeNow()
    }

    @Test
    fun `generate audio segments for 'background noise low volume' file`(testContext: VertxTestContext) = runBlocking {
        generateAudioSegments(
            sourceAudioFileInfoForTest = File("$audioSegmentsPath/background-noise-low-volume-file-info.json"),
            audioContentInfoFileForTest = File("$audioSegmentsPath/background-noise-low-volume-content-info.json"),
            sourceAudioFileForTest = File("$audioSegmentsPath/background-noise-low-volume.wav"),
            testContext = testContext)
    }

    @Test
    fun `generate audio segments for 'strong background noise' file`(testContext: VertxTestContext) = runBlocking {
        generateAudioSegments(
            sourceAudioFileInfoForTest = File("$audioSegmentsPath/strong-background-noise-file-info.json"),
            audioContentInfoFileForTest = File("$audioSegmentsPath/strong-background-noise-content-info.json"),
            sourceAudioFileForTest = File("$audioSegmentsPath/strong-background-noise.wav"),
            testContext = testContext)
    }

    @Test
    fun `generate audio segments for 'with applause' file`(testContext: VertxTestContext) = runBlocking {
        generateAudioSegments(
            sourceAudioFileInfoForTest = File("$audioSegmentsPath/with-applause-file-info.json"),
            audioContentInfoFileForTest = File("$audioSegmentsPath/with-applause-content-info.json"),
            sourceAudioFileForTest = File("$audioSegmentsPath/with-applause.wav"),
            testContext = testContext)
    }

    private suspend fun generateAudioSegments(
        sourceAudioFileInfoForTest: File,
        audioContentInfoFileForTest: File,
        sourceAudioFileForTest: File,
        testContext: VertxTestContext
    ) = withContext(Dispatchers.IO) {
        val mockSourceFileInfo = mapper.readValue(sourceAudioFileInfoForTest, AudioSourceFileInfo::class.java)

        When(sourceFileInfoRepository.findBy(sourceAudioFileForTest.nameWithoutExtension)).thenReturn(mockSourceFileInfo)

        audioSegmentsService.generateAudioSegments(
            audioContentInfo = mapper.readValue(audioContentInfoFileForTest, AudioContentInfo::class.java),
            audioFile = sourceAudioFileForTest)
        eventHandler.assertAudioSegments(sourceAudioFileForTest.nameWithoutExtension, testContext)
    }
}

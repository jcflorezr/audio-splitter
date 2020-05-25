package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audioclips.AudioClipsFilesGeneratorImplCpSpecDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.core.util.JsonUtils.fromJsonToList
import net.jcflorezr.transcriber.core.util.JsonUtils.fromJsonToObject
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when` as When

@ObsoleteCoroutinesApi
@ExtendWith(VertxExtension::class)
internal class AudioClipsFilesGeneratorImplCpSpec {

    private val audioClipsFilesPath = this.javaClass.getResource("/audio-clips").path
    private val audioSegmentsFilesPath = this.javaClass.getResource("/audio-segments").path

    private val audioClipsFilesGeneratorImplCpSpecDI = AudioClipsFilesGeneratorImplCpSpecDI
    private val eventHandler = audioClipsFilesGeneratorImplCpSpecDI.audioClipFileGeneratedEventHandler()
    private val audioClipsFilesGenerator = audioClipsFilesGeneratorImplCpSpecDI.audioClipsFilesGenerator
    private val sourceFileInfoRepository = audioClipsFilesGeneratorImplCpSpecDI.sourceFileInfoRepositoryMock()
    private val audioSegmentsRepository = audioClipsFilesGeneratorImplCpSpecDI.audioSegmentsRepositoryMock()

    @BeforeAll
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(AudioClipsFilesGeneratorImplCpSpecDI, testContext.completing())
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
        testContext.completeNow()
    }

    @Test
    fun `generate audio clips files for 'background noise low volume' segments`(testContext: VertxTestContext) = runBlocking {
        generateAudioClipsFiles(sourceAudioFileName = "background-noise-low-volume", testContext = testContext)
    }

    @Test
    fun `generate audio clips files for 'strong background noise' segments`(testContext: VertxTestContext) = runBlocking {
        generateAudioClipsFiles(sourceAudioFileName = "strong-background-noise", testContext = testContext)
    }

    @Test
    fun `generate audio clips files for 'with applause' segments`(testContext: VertxTestContext) = runBlocking {
        generateAudioClipsFiles(sourceAudioFileName = "with-applause", testContext = testContext)
    }

    private suspend fun generateAudioClipsFiles(sourceAudioFileName: String, testContext: VertxTestContext) = withContext(Dispatchers.IO) {
        val audioSourceFileInfo =
            fromJsonToObject<AudioSourceFileInfo>(File("$audioSegmentsFilesPath/$sourceAudioFileName-file-info.json"))
        val audioSegments =
            fromJsonToList<AudioSegment>(jsonFile = File("$audioSegmentsFilesPath/$sourceAudioFileName-audio-segments.json"))
        val audioClipsInfo =
            fromJsonToList<AudioClip>(jsonFile = File("$audioClipsFilesPath/$sourceAudioFileName-audio-clips.json"))

        When(sourceFileInfoRepository.findBy(sourceAudioFileName)).thenReturn(audioSourceFileInfo)

        audioClipsInfo.forEach { audioClipInfo ->
            val firstSegment = audioClipInfo.activeSegments.first()
            val lastSegment = audioClipInfo.activeSegments.last()
            val from = audioSegments.binarySearchBy(firstSegment.segmentStartInSeconds) { it.segmentStartInSeconds }
            val to = audioSegments.binarySearchBy(lastSegment.segmentEndInSeconds) { it.segmentStartInSeconds }
                .let { if (it >= audioSegments.size) { it } else { it + 1 } }
            When(audioSegmentsRepository.findSegmentsRange(sourceAudioFileName, firstSegment.segmentStartInSeconds, lastSegment.segmentEndInSeconds))
                .thenReturn(audioSegments.subList(from, to))
            audioClipsFilesGenerator.generateAudioClipFile(audioClipInfo)
        }
        eventHandler.assertAudioClipsFiles(sourceAudioFileName, testContext)
    }
}

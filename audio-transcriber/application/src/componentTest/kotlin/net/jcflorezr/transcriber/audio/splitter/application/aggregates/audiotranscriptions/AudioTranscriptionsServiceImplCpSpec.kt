package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.io.File
import java.io.FileNotFoundException
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import net.jcflorezr.transcriber.audio.splitter.application.di.aggregates.audiotranscriptions.AudioTranscriptionsServiceImplCpSpecDI
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.Alternative
import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo
import net.jcflorezr.transcriber.core.exception.FileException
import net.jcflorezr.transcriber.core.util.JsonUtils.fromJsonToList
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when` as When

@ObsoleteCoroutinesApi
@ExtendWith(VertxExtension::class)
internal class AudioTranscriptionsServiceImplCpSpec {

    private val audioClipsTranscriptionsPath = this.javaClass.getResource("/audio-clips-transcriptions").path
    private val audioTranscriptionServiceImplCpSpecDI = AudioTranscriptionsServiceImplCpSpecDI
    private val audioClipsFilesPath = audioTranscriptionServiceImplCpSpecDI.clipsDirectory()
    private val audioTranscriptionsClient = audioTranscriptionServiceImplCpSpecDI.audioTranscriptionsMockClient()
    private val audioTranscriptionsService = audioTranscriptionServiceImplCpSpecDI.audioTranscriptionsServiceTest()
    private val audioTranscriptionDummyEvent = audioTranscriptionServiceImplCpSpecDI.audioTranscriptionGeneratedEventHandler()

    @BeforeAll
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(audioTranscriptionServiceImplCpSpecDI, testContext.completing())
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
        testContext.completeNow()
    }

    @Test
    fun `generate AudioTranscriptionGenerated events, receive them and store their content in db`(
        testContext: VertxTestContext
    ) = runBlocking {
        val filesKeyword = "aggregate"
        val testDirectory = File(audioClipsTranscriptionsPath).takeIf { it.exists() }
            ?: throw FileException.fileNotFound(audioClipsTranscriptionsPath)
        val expectedAudioTranscriptionsFiles =
            testDirectory.listFiles { file -> !file.nameWithoutExtension.contains(filesKeyword) }?.takeIf { it.isNotEmpty() }
            ?: throw FileNotFoundException("No files with keyword '$filesKeyword' were found in directory '$audioClipsTranscriptionsPath'")

        expectedAudioTranscriptionsFiles
            .map { generatedAudioClipFile ->
                // Given
                val dummyGeneratedAudioClip = createDummyGeneratedAudioClip(generatedAudioClipFile)
                val alternatives = fromJsonToList<Alternative>(
                    jsonFile = File("$audioClipsTranscriptionsPath/${dummyGeneratedAudioClip.audioClipFileName}.json")
                )

                // When
                When(audioTranscriptionsClient.getAudioTranscriptionAlternatives(
                    "$audioClipsFilesPath/${generatedAudioClipFile.name}"))
                    .thenReturn(alternatives)

                // Then
                audioTranscriptionsService.transcribe(dummyGeneratedAudioClip)
                dummyGeneratedAudioClip
            }.let {
                audioTranscriptionDummyEvent.assertAudioTranscriptions(it.first().sourceAudioFileName, testContext)
            }
    }

    private fun createDummyGeneratedAudioClip(generatedAudioClipFile: File): AudioClipFileInfo {
        val clipFileName = generatedAudioClipFile.nameWithoutExtension
        val clipFileExtension = generatedAudioClipFile.extension
        val hours = clipFileName.substringBefore("_").toInt() / 3600
        val minutes = clipFileName.substringBefore("_").toInt() % 3600 / 60
        val seconds = clipFileName.substringBefore("_").toInt() % 60
        val tenths = clipFileName.substringAfter("_").substringBefore("_").toInt()
        return AudioClipFileInfo.createNew(
            sourceAudioFileName = "test-source-audio-file-name",
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            tenthsOfSecond = tenths,
            audioClipFileName = clipFileName,
            audioClipFileExtension = clipFileExtension)
    }
}

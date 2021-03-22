package net.jcflorezr.transcriber.audio.transcriber.adapters.cloud.speech

import java.io.File
import java.io.FileNotFoundException
import kotlinx.coroutines.runBlocking
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.cloud.speech.GoogleCloudSpeechClientTestDI
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.dto.GoogleCloudTranscriptionAlternativeDto
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.Alternative
import net.jcflorezr.transcriber.core.util.JsonUtils.fromJsonToList
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when` as When

internal class GoogleCloudAudioTranscriptionsClientTest {

    private val googleCloudSpeechClientTestDI = GoogleCloudSpeechClientTestDI()
    private val googleAudioTranscriptionsClient = googleCloudSpeechClientTestDI.googleCloudAudioTranscriptionsClientTest()
    private val googleSpeechApiClient = googleCloudSpeechClientTestDI.googleSpeechApiClientMock()
    private val googleRecognitionConfig = googleCloudSpeechClientTestDI.googleRecognitionConfigMock()
    private val googleRecognitionAudioConfig = googleCloudSpeechClientTestDI.googleRecognitionAudioConfigMock()

    private val audioClipsFilesPath = this.javaClass.getResource("/audio-clips-files").path
    private val audioClipsTranscriptionsPath = this.javaClass.getResource("/audio-clips-transcriptions").path

    @Test
    fun getAudioTranscriptions() = runBlocking {
        File(audioClipsFilesPath)
            .takeIf { it.exists() }
            ?.listFiles()
            ?.asSequence()
            ?.forEach { audioClipFile ->
                // Given
                val audioClipFilePath = audioClipFile.absolutePath
                val alternativesDto = fromJsonToList<GoogleCloudTranscriptionAlternativeDto>(
                    jsonFile = File("$audioClipsTranscriptionsPath/${audioClipFile.nameWithoutExtension}_dto.json")
                )
                val expectedAlternatives = fromJsonToList<Alternative>(
                    jsonFile = File("$audioClipsTranscriptionsPath/${audioClipFile.nameWithoutExtension}_entity.json")
                )

                // When
                When(googleSpeechApiClient.recognize(audioClipFilePath, googleRecognitionConfig, googleRecognitionAudioConfig))
                    .thenReturn(alternativesDto.asSequence())

                // Then
                googleAudioTranscriptionsClient.getAudioTranscriptionAlternatives(audioClipFilePath)
                    .forEachIndexed { i, actualTranscription ->
                        assertThat(actualTranscription, Is(equalTo(expectedAlternatives[i])))
                    }
            }
            ?: throw FileNotFoundException("Directory '$audioClipsFilesPath' was not found")
    }
}

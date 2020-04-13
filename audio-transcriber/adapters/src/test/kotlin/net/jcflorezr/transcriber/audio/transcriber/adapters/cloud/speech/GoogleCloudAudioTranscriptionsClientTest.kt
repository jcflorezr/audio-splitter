package net.jcflorezr.transcriber.audio.transcriber.adapters.cloud.speech

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.io.FileNotFoundException
import kotlinx.coroutines.runBlocking
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.GoogleCloudSpeechClientTestDI
import net.jcflorezr.transcriber.audio.transcriber.adapters.dto.GoogleCloudTranscriptionAlternativeDto
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.Alternative
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.CoreMatchers.`is` as Is
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when` as When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [GoogleCloudSpeechClientTestDI::class])
internal class GoogleCloudAudioTranscriptionsClientTest {

    @Autowired
    private lateinit var googleAudioTranscriptionsClient: AudioTranscriptionsClient
    @Autowired
    private lateinit var googleSpeechApiClient: GoogleSpeechApiClient
    @Autowired
    private lateinit var googleRecognitionConfig: GoogleRecognitionConfig
    @Autowired
    private lateinit var googleRecognitionAudioConfig: GoogleRecognitionAudioConfig

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<GoogleCloudAudioTranscriptionsClientTest> = this.javaClass
    private val audioClipsFilesPath: String
    private val audioClipsTranscriptionsPath: String

    init {
        audioClipsFilesPath = thisClass.getResource("/audio-clips-files").path
        audioClipsTranscriptionsPath = thisClass.getResource("/audio-clips-transcriptions").path
    }

    @Test
    fun getAudioTranscriptions() = runBlocking {
        File(audioClipsFilesPath)
            .takeIf { it.exists() }
            ?.listFiles()
            ?.asSequence()
            ?.forEach { audioClipFile ->
                // Given
                val audioClipFilePath = audioClipFile.absolutePath

                val transcriptionAlternativeDtoListType = MAPPER.typeFactory
                    .constructCollectionType(List::class.java, GoogleCloudTranscriptionAlternativeDto::class.java)
                val transcriptionAlternativesFile =
                    File("$audioClipsTranscriptionsPath/${audioClipFile.nameWithoutExtension}_dto.json")
                val alternativesDto: List<GoogleCloudTranscriptionAlternativeDto> =
                    MAPPER.readValue(transcriptionAlternativesFile, transcriptionAlternativeDtoListType)

                val transcriptionAlternativesEntityListType = MAPPER.typeFactory
                    .constructCollectionType(List::class.java, Alternative::class.java)
                val transcriptionAlternativesEntityFile =
                    File("$audioClipsTranscriptionsPath/${audioClipFile.nameWithoutExtension}_entity.json")
                val expectedAlternatives: List<Alternative> =
                    MAPPER.readValue(transcriptionAlternativesEntityFile, transcriptionAlternativesEntityListType)

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
package net.jcflorezr.transcriber.audio.transcriber.adapters.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.dto.GoogleCloudTranscriptionAlternativeDto
import java.io.File
import java.io.FileNotFoundException
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.Alternative
import net.jcflorezr.transcriber.core.exception.FileException
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GoogleCloudAlternativeAlternativeDtoTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<GoogleCloudAlternativeAlternativeDtoTest> = this.javaClass
    private val audioClipsFilesPath: String
    private val audioClipsTranscriptionsPath: String

    init {
        audioClipsFilesPath = thisClass.getResource("/audio-clips-files").path
        audioClipsTranscriptionsPath = thisClass.getResource("/audio-clips-transcriptions").path
    }

    @Test
    fun testingTranslationFromDtoToEntity() {
        File(audioClipsFilesPath)
        .takeIf { it.exists() }
        ?.listFiles()
        ?.asSequence()
        ?.forEach { audioClipFile ->
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

            assertThat(alternativesDto.size, Is(equalTo(expectedAlternatives.size)))
            alternativesDto.forEachIndexed { index, alternativeDto ->
                assertThat(alternativeDto.toEntity(), Is(equalTo(expectedAlternatives[index])))
            }
        } ?: throw FileException.fileNotFound(audioClipsFilesPath)
    }
}
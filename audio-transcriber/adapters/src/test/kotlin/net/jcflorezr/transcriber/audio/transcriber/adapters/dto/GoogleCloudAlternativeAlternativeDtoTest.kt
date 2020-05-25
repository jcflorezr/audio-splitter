package net.jcflorezr.transcriber.audio.transcriber.adapters.dto

import java.io.File
import net.jcflorezr.transcriber.audio.transcriber.adapters.ports.cloud.speech.dto.GoogleCloudTranscriptionAlternativeDto
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.Alternative
import net.jcflorezr.transcriber.core.exception.FileException
import net.jcflorezr.transcriber.core.util.JsonUtils.fromJsonToList
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Test

internal class GoogleCloudAlternativeAlternativeDtoTest {

    private val audioClipsFilesPath = this.javaClass.getResource("/audio-clips-files").path
    private val audioClipsTranscriptionsPath = this.javaClass.getResource("/audio-clips-transcriptions").path

    @Test
    fun `translate from dto to entity`() {
        File(audioClipsFilesPath)
        .takeIf { it.exists() }
        ?.listFiles()
        ?.asSequence()
        ?.forEach { audioClipFile ->
            val alternativesDto = fromJsonToList<GoogleCloudTranscriptionAlternativeDto>(
                jsonFile = File("$audioClipsTranscriptionsPath/${audioClipFile.nameWithoutExtension}_dto.json"))
            val expectedAlternatives = fromJsonToList<Alternative>(
                jsonFile = File("$audioClipsTranscriptionsPath/${audioClipFile.nameWithoutExtension}_entity.json"))

            assertThat(alternativesDto.size, Is(equalTo(expectedAlternatives.size)))
            alternativesDto.forEachIndexed { index, alternativeDto ->
                assertThat(alternativeDto.toEntity(), Is(equalTo(expectedAlternatives[index])))
            }
        } ?: throw FileException.fileNotFound(audioClipsFilesPath)
    }
}

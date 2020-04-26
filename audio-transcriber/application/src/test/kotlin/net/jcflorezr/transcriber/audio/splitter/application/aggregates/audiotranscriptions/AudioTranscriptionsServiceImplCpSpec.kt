package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import net.jcflorezr.transcriber.audio.splitter.application.di.AudioTranscriptionServiceImplCpSpecDI
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.Alternative
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.aggregates.application.audiotranscriptions.AudioTranscriptionsService
import net.jcflorezr.transcriber.audio.transcriber.domain.ports.cloud.speech.AudioTranscriptionsClient
import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo
import net.jcflorezr.transcriber.core.exception.FileException
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when` as When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.io.FileNotFoundException

@ObsoleteCoroutinesApi
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AudioTranscriptionServiceImplCpSpecDI::class])
internal class AudioTranscriptionsServiceImplCpSpec {

    @Autowired
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var audioTranscriptionsService: AudioTranscriptionsService
    @Autowired
    private lateinit var audioTranscriptionsClient: AudioTranscriptionsClient
    @Autowired
    private lateinit var audioTranscriptionServiceImplCpSpecDI: AudioTranscriptionServiceImplCpSpecDI

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioTranscriptionsServiceImplCpSpec> = this.javaClass
    private val audioClipsTranscriptionsPath: String
    init {
        audioClipsTranscriptionsPath = thisClass.getResource("/audio-clips-transcriptions").path
    }

    @Test
    fun transcribeAudioClipsContent() = runBlocking {
        val audioClipsFilesPath = audioTranscriptionServiceImplCpSpecDI.clipsDirectory()
        val filesKeyword = "aggregate"

        val testDirectory = File(audioClipsTranscriptionsPath).takeIf { it.exists() }
            ?: throw FileException.fileNotFound(audioClipsTranscriptionsPath)
        val expectedAudioTranscriptionsFiles =
            testDirectory.listFiles { file -> !file.nameWithoutExtension.contains(filesKeyword) }?.takeIf { it.isNotEmpty() }
            ?: throw FileNotFoundException("No files with keyword '$filesKeyword' were found in directory '$audioClipsTranscriptionsPath'")

        expectedAudioTranscriptionsFiles.asSequence()
            .forEach { generatedAudioClipFile ->
                // Given
                val dummyGeneratedAudioClip = createDummyGeneratedAudioClip(generatedAudioClipFile)

                val transcriptionAlternativesListType = MAPPER.typeFactory
                    .constructCollectionType(List::class.java, Alternative::class.java)
                val transcriptionAlternativesFile =
                    File("$audioClipsTranscriptionsPath/${dummyGeneratedAudioClip.audioClipFileName}.json")
                val alternatives: List<Alternative> =
                    MAPPER.readValue(transcriptionAlternativesFile, transcriptionAlternativesListType)

                // When
                When(audioTranscriptionsClient.getAudioTranscriptionAlternatives(
                    "$audioClipsFilesPath/${generatedAudioClipFile.name}"))
                    .thenReturn(alternatives)

                // Then
                audioTranscriptionsService.transcribe(dummyGeneratedAudioClip)
            }

        val audioClipsDummyCommand =
            applicationCtx.getBean("audioTranscriptionDummyCommand") as AudioTranscriptionDummyCommand
        audioClipsDummyCommand.assertAudioTranscriptions()
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
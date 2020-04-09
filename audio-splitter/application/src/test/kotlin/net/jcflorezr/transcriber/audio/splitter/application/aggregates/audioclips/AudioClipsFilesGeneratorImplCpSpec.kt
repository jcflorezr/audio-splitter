package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.application.di.AudioClipsFilesGeneratorImplCpSpecDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.AudioSegmentsRepository
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import org.mockito.Mockito.`when` as When

@ObsoleteCoroutinesApi
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AudioClipsFilesGeneratorImplCpSpecDI::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
internal class AudioClipsFilesGeneratorImplCpSpec {

    @Autowired
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var audioClipsFilesGenerator: AudioClipsFilesGenerator
    @Autowired
    private lateinit var sourceFileInfoRepository: SourceFileInfoRepository
    @Autowired
    private lateinit var audioSegmentsRepository: AudioSegmentsRepository

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioClipsFilesGeneratorImplCpSpec> = this.javaClass
    private val audioClipsFilesPath: String
    private val audioSegmentsFilesPath: String

    init {
        audioClipsFilesPath = thisClass.getResource("/audio-clips").path
        audioSegmentsFilesPath = thisClass.getResource("/audio-segments").path
    }

    @Test
    fun generateAudioClipsFilesFor_BackgroundNoiseLowVolume_Segments() = runBlocking {
        generateAudioClipsFiles(sourceAudioFileName = "background-noise-low-volume")
    }

    @Test
    fun generateAudioClipsFilesFor_StrongBackgroundNoise_Segments() = runBlocking {
        generateAudioClipsFiles(sourceAudioFileName = "strong-background-noise")
    }

    @Test
    fun generateAudioClipsFilesFor_WithApplause_Segments() = runBlocking {
        generateAudioClipsFiles(sourceAudioFileName = "with-applause")
    }

    private suspend fun generateAudioClipsFiles(sourceAudioFileName: String) = withContext(Dispatchers.IO) {
        val audioSourceFileInfoPath = "$audioSegmentsFilesPath/$sourceAudioFileName-file-info.json"
        val audioSourceFileInfo = MAPPER.readValue(File(audioSourceFileInfoPath), AudioSourceFileInfo::class.java)

        When(sourceFileInfoRepository.findBy(sourceAudioFileName)).thenReturn(audioSourceFileInfo)

        val audioClipsPath = "$audioClipsFilesPath/$sourceAudioFileName-audio-clips.json"
        val audioClipListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClip::class.java)
        val audioClips = MAPPER.readValue(File(audioClipsPath), audioClipListType) as List<AudioClip>

        val audioSegmentsPath = "$audioSegmentsFilesPath/$sourceAudioFileName-audio-segments.json"
        val audioSegmentsListType =
            MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSegment::class.java)
        val audioSegments = MAPPER.readValue(File(audioSegmentsPath), audioSegmentsListType) as List<AudioSegment>

        audioClips.forEach { audioClipInfo ->
            val firstSegment = audioClipInfo.activeSegments.first()
            val lastSegment = audioClipInfo.activeSegments.last()
            val from = audioSegments.binarySearchBy(firstSegment.segmentStart) { it.segmentStartInSeconds }
            val to = audioSegments.binarySearchBy(lastSegment.segmentEnd) { it.segmentStartInSeconds }
                .let { if (it >= audioSegments.size) { it } else { it + 1 } }
            When(audioSegmentsRepository.findBy(sourceAudioFileName, firstSegment.segmentStart, lastSegment.segmentEnd))
                .thenReturn(audioSegments.subList(from, to))
            audioClipsFilesGenerator.generateAudioClipFile(audioClipInfo)
        }

        val audioClipsDummyCommand =
            applicationCtx.getBean("audioClipsFilesGeneratorDummyCommand") as AudioClipsFilesGeneratorDummyCommand
        audioClipsDummyCommand.assertAudioClipsGenerated()
    }
}
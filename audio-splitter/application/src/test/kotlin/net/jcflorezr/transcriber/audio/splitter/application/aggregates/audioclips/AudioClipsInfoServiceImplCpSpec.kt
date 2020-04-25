package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.application.commands.audioclips.AudioClipsDummyCommand
import net.jcflorezr.transcriber.audio.splitter.application.di.AudioClipsServiceImplCpSpecDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioClipsInfoService
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when` as When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File

@ObsoleteCoroutinesApi
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AudioClipsServiceImplCpSpecDI::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
internal class AudioClipsInfoServiceImplCpSpec {

    @Autowired
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var audioClipsInfoService: AudioClipsInfoService
    @Autowired
    private lateinit var sourceFileInfoRepository: SourceFileInfoRepository

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioClipsInfoServiceImplCpSpec> = this.javaClass
    private val segmentsSourceFilesPath: String

    init {
        segmentsSourceFilesPath = thisClass.getResource("/audio-segments").path
    }

    @Test
    fun generateAudioClipsFor_BackgroundNoiseLowVolume_Segments() = runBlocking {
        generateAudioClips(sourceAudioFileName = "background-noise-low-volume")
    }

    @Test
    fun generateAudioClipsFor_StrongBackgroundNoise_Segments() = runBlocking {
        generateAudioClips(sourceAudioFileName = "strong-background-noise")
    }

    @Test
    fun generateAudioClipsFor_WithApplause_Segments() = runBlocking {
        generateAudioClips(sourceAudioFileName = "with-applause")
    }

    private suspend fun generateAudioClips(sourceAudioFileName: String) = withContext(Dispatchers.IO) {
        val audioSourceFileInfoPath = "$segmentsSourceFilesPath/$sourceAudioFileName-file-info.json"
        val audioSourceFileInfo = MAPPER.readValue(File(audioSourceFileInfoPath), AudioSourceFileInfo::class.java)

        When(sourceFileInfoRepository.findBy(sourceAudioFileName)).thenReturn(audioSourceFileInfo)

        val audioSegmentsPath = "$segmentsSourceFilesPath/$sourceAudioFileName-audio-segments.json"
        val audioClipListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSegment::class.java)
        val audioSegments = (MAPPER.readValue(File(audioSegmentsPath), audioClipListType) as List<AudioSegment>)
            .map { BasicAudioSegment.fromAudioSegment(it) }

        audioClipsInfoService.generateActiveSegments(audioSegments)

        val audioClipsDummyCommand = applicationCtx.getBean("audioClipsDummyCommand") as AudioClipsDummyCommand
        audioClipsDummyCommand.assertAudioClips()
    }
}
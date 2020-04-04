package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.application.di.AudioSegmentsServiceImplCpSpecDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File

@ObsoleteCoroutinesApi
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AudioSegmentsServiceImplCpSpecDI::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
internal class AudioSegmentsServiceImplCpSpec {

    @Autowired
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var audioSegmentsService: AudioSegmentsService

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioSegmentsServiceImplCpSpec> = this.javaClass
    private val audioSegmentsPath: String

    init {
        audioSegmentsPath = thisClass.getResource("/audio-segments").path
    }

    @Test
    fun generateAudioSegmentsFor_BackgroundNoiseLowVolume_File() = runBlocking {
        generateAudioSegments(
            audioInfoPath = "$audioSegmentsPath/background-noise-low-volume-content-info.json",
            audioFilePath = "$audioSegmentsPath/background-noise-low-volume.wav")
    }

    @Test
    fun generateAudioSegmentsFor_StrongBackgroundNoise_File() = runBlocking {
        generateAudioSegments(
            audioInfoPath = "$audioSegmentsPath/strong-background-noise-content-info.json",
            audioFilePath = "$audioSegmentsPath/strong-background-noise.wav")
    }

    @Test
    fun generateAudioSegmentsFor_WithApplause_File() = runBlocking {
        generateAudioSegments(
            audioInfoPath = "$audioSegmentsPath/with-applause-content-info.json",
            audioFilePath = "$audioSegmentsPath/with-applause.wav")
    }

    private suspend fun generateAudioSegments(audioInfoPath: String, audioFilePath: String) = withContext(Dispatchers.IO) {
        audioSegmentsService.generateAudioSegments(
            audioContentInfo = MAPPER.readValue(File(audioInfoPath), AudioContentInfo::class.java),
            audioFile = File(audioFilePath))
        val audioSegmentsDummyCommand =
            applicationCtx.getBean("audioSegmentsDummyCommand") as AudioSegmentsDummyCommand
        audioSegmentsDummyCommand.assertAudioSegments()
    }
}
package net.jcflorezr.signal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.TestSoundZonesDetectorConfig
import net.jcflorezr.model.AudioSignalRmsInfoKt
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestSoundZonesDetectorConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SoundZonesDetectorTest {

    @Autowired
    private lateinit var soundZonesDetector: SoundZonesDetector

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val signalResourcesPath: String
    private val clipsResourcesPath: String
    private val thisClass: Class<SoundZonesDetectorTest> = this.javaClass

    init {
        signalResourcesPath = thisClass.getResource("/signal/").path
        clipsResourcesPath = thisClass.getResource("/clip/").path
    }

    @Test
    fun generateAudioClipsInfoForFileWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        detectSoundZones(
            path = signalResourcesPath + "background-noise-low-volume/background-noise-low-volume.json"
        )
    }

    @Test
    fun generateAudioClipsInfoForFileWithApplause() = runBlocking {
        detectSoundZones(
            path = signalResourcesPath + "with-applause/with-applause.json"
        )
    }

    @Test
    fun generateAudioClipsInfoForFileWithStrongBackgroundNoise() = runBlocking {
        detectSoundZones(
            path = signalResourcesPath + "strong-background-noise/strong-background-noise.json"
        )
    }

    private suspend fun detectSoundZones(path: String) {
        val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfoKt::class.java)
        val audioSignalRmsList: List<AudioSignalRmsInfoKt> = MAPPER.readValue(File(path), signalRmsListType)
        soundZonesDetector.detectSoundZones(audioRmsInfoList = audioSignalRmsList)
    }
}
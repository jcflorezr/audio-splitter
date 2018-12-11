package net.jcflorezr.signal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.config.TestRootConfig
import net.jcflorezr.model.AudioSignalRmsInfoKt
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File

@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestRootConfig::class])
class SoundZonesDetectorImplTest {

    @Autowired
    private lateinit var soundZonesDetector: SoundZonesDetector

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val testResourcesPath: String
    private val thisClass: Class<SoundZonesDetectorImplTest> = this.javaClass

    init {
        testResourcesPath = thisClass.getResource("/signal/").path
    }

    @Test
    fun generateAudioClipsInfoForFileWithBackgroundNoiseAndLowVoiceVolume() {
        generateRmsInfo(path = testResourcesPath + "background-noise-low-volume/background-noise-low-volume.json")
    }

    @Test
    fun generateAudioClipsInfoForFileWithApplause() {
        generateRmsInfo(path = testResourcesPath + "with-applause/with-applause.json")
    }

    @Test
    fun generateAudioClipsInfoForFileWithStrongBackgroundNoise() {
        generateRmsInfo(path = testResourcesPath + "strong-background-noise/strong-background-noise.json")
    }

    private fun generateRmsInfo(path: String) {
        val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfoKt::class.java)
        val audioSignalRmsList: List<AudioSignalRmsInfoKt> = MAPPER.readValue(File(path), signalRmsListType)
        soundZonesDetector.getSoundZones(audioRmsInfoList = audioSignalRmsList)
        Thread.sleep(2000L)
    }
}
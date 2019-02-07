package net.jcflorezr.signal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.config.TestRootConfig
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfoKt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import org.hamcrest.CoreMatchers.`is` as Is

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestRootConfig::class])
class SoundZonesDetectorImplTest {

    @Autowired
    private lateinit var soundZonesDetector: SoundZonesDetector

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val signalResourcesPath: String
    private val clipsResourcesPath: String
    private val thisClass: Class<SoundZonesDetectorImplTest> = this.javaClass

    init {
        signalResourcesPath = thisClass.getResource("/signal/").path
        clipsResourcesPath = thisClass.getResource("/clip/").path
    }

    @Test
    fun generateAudioClipsInfoForFileWithBackgroundNoiseAndLowVoiceVolume() {
        detectSoundZones(
            path = signalResourcesPath + "background-noise-low-volume/background-noise-low-volume.json",
            folderName = "background-noise-low-volume"
        )
    }

//    @Test
//    fun generateIncompleteAudioClipsInfoForFileWithBackgroundNoiseAndLowVoiceVolume() {
//        detectSoundZones(
//            path = signalResourcesPath + "background-noise-low-volume/background-noise-low-volume-incomplete.json",
//            folderName = "background-noise-low-volume"
//        )
//    }

    @Test
    fun generateAudioClipsInfoForFileWithApplause() {
        detectSoundZones(
            path = signalResourcesPath + "with-applause/with-applause.json",
            folderName = "with-applause"
        )
    }

//    @Test
//    fun generateIncompleteAudioClipsInfoForFileWithApplause() {
//        detectSoundZones(
//            path = signalResourcesPath + "with-applause/with-applause-incomplete.json",
//            folderName = "with-applause"
//        )
//    }

    @Test
    fun generateAudioClipsInfoForFileWithStrongBackgroundNoise() {
        detectSoundZones(
            path = signalResourcesPath + "strong-background-noise/strong-background-noise.json",
            folderName = "strong-background-noise"
        )
    }

    private fun detectSoundZones(path: String, folderName: String) {
//        val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfoKt::class.java)
//        val audioSignalRmsList: List<AudioSignalRmsInfoKt> = MAPPER.readValue(File(path), signalRmsListType)
//        val actualAudioClipsInfoList = soundZonesDetector.generateSoundZones(audioRmsInfoList = audioSignalRmsList)
//        val audioClipListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
//        val expectedAudioClipsInfoList: List<AudioClipInfo> =
//            MAPPER.readValue(File("$clipsResourcesPath/$folderName/$folderName.json"), audioClipListType)
//        assertThat(actualAudioClipsInfoList, Is(equalTo(expectedAudioClipsInfoList)))
    }
}
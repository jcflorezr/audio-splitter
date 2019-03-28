package net.jcflorezr.rms

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.broker.AudioClipInfoSubscriberMock
import net.jcflorezr.config.TestSoundZonesDetectorConfig
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.util.PropsUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
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
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var soundZonesDetector: SoundZonesDetector

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val signalResourcesPath: String
    private val clipsResourcesPath: String
    private val thisClass: Class<SoundZonesDetectorTest> = this.javaClass

    init {
        signalResourcesPath = thisClass.getResource("/rms/").path
        clipsResourcesPath = thisClass.getResource("/clip/").path
    }

    @Test
    fun generateAudioClipsInfoForFileWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("background-noise-low-volume"))
        detectSoundZones(
            path = signalResourcesPath + "background-noise-low-volume/background-noise-low-volume.json"
        )
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockTest") as AudioClipInfoSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Test
    fun generateAudioClipsInfoForFileWithApplause() = runBlocking {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("with-applause"))
        detectSoundZones(
            path = signalResourcesPath + "with-applause/with-applause.json"
        )
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockTest") as AudioClipInfoSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Test
    fun generateAudioClipsInfoForFileWithStrongBackgroundNoise() = runBlocking {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("strong-background-noise"))
        detectSoundZones(
            path = signalResourcesPath + "strong-background-noise/strong-background-noise.json"
        )
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockTest") as AudioClipInfoSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    private suspend fun detectSoundZones(path: String) {
        val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfo::class.java)
        val audioSignalRmsList: List<AudioSignalRmsInfo> = MAPPER.readValue(File(path), signalRmsListType)
        soundZonesDetector.detectSoundZones(audioRmsInfoList = audioSignalRmsList)
    }
}
package net.jcflorezr.clip

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.broker.AudioClipSignalSubscriberMock
import net.jcflorezr.config.TestClipsGeneratorConfig
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.TestRedisInitializer
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.util.PropsUtils
import org.junit.ClassRule
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
@ContextConfiguration(classes = [TestClipsGeneratorConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClipGeneratorIntegrationTest {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var clipGenerator: ClipGenerator
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = TestRedisInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val signalResourcesPath: String
    private val clipsResourcesPath: String
    private val thisClass: Class<ClipGeneratorIntegrationTest> = this.javaClass

    init {
        signalResourcesPath = thisClass.getResource("/signal/").path
        clipsResourcesPath = thisClass.getResource("/clip/").path
    }

    @Test
    fun generateAudioClipsForFileWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume"))
        generateAudioClips(
            signalsFolderPath = signalResourcesPath + "background-noise-low-volume/",
            clipsPath = clipsResourcesPath + "background-noise-low-volume/background-noise-low-volume.json"
        )
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

    @Test
    fun generateAudioClipsInfoForFileWithApplause() = runBlocking {
        propsUtils.setTransactionId(sourceAudioFile = File("with-applause"))
        generateAudioClips(
            signalsFolderPath = signalResourcesPath + "with-applause/",
            clipsPath = clipsResourcesPath + "with-applause/with-applause.json"
        )
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

    @Test
    fun generateAudioClipsInfoForFileWithStrongBackgroundNoise() = runBlocking {
        propsUtils.setTransactionId(sourceAudioFile = File("strong-background-noise"))
        generateAudioClips(
            signalsFolderPath = signalResourcesPath + "strong-background-noise/",
            clipsPath = clipsResourcesPath + "strong-background-noise/strong-background-noise.json"
        )
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

    @Test
    fun generateIncompleteAudioClipsForFileWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume-incomplete"))
        generateAudioClips(
            signalsFolderPath = signalResourcesPath + "background-noise-low-volume-incomplete/",
            clipsPath = clipsResourcesPath + "background-noise-low-volume-incomplete/background-noise-low-volume-incomplete.json"
        )
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

    private suspend fun generateAudioClips(clipsPath: String, signalsFolderPath: String) {
        File(signalsFolderPath).listFiles()
        .filter { it.extension == "json" }
        .forEach { signalFile ->
            val audioSignal = MAPPER.readValue(signalFile, AudioSignal::class.java)
            audioSignalDao.storeAudioSignal(audioSignal)
        }
        val clipInfoListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
        val audioClipInfoList: List<AudioClipInfo> = MAPPER.readValue(File(clipsPath), clipInfoListType)
        clipGenerator.generateClips(audioClipsInfo = audioClipInfoList)
    }
}
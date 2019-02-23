package net.jcflorezr.clip

import biz.source_code.dsp.model.AudioSignalKt
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.broker.AudioClipSignalSubscriberMock
import net.jcflorezr.config.TestClipsGeneratorConfig
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.RedisInitializer
import net.jcflorezr.model.AudioClipInfo
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
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var clipGenerator: ClipGenerator
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = RedisInitializer()
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
        generateAudioClips(
            signalsFolderPath = signalResourcesPath + "background-noise-low-volume/",
            clipsPath = clipsResourcesPath + "background-noise-low-volume/background-noise-low-volume.json"
        )
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSignalSubscriberTest") as AudioClipSignalSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Test
    fun generateAudioClipsInfoForFileWithApplause() = runBlocking {
        generateAudioClips(
            signalsFolderPath = signalResourcesPath + "with-applause/",
            clipsPath = clipsResourcesPath + "with-applause/with-applause.json"
        )
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSignalSubscriberTest") as AudioClipSignalSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Test
    fun generateAudioClipsInfoForFileWithStrongBackgroundNoise() = runBlocking {
        generateAudioClips(
            signalsFolderPath = signalResourcesPath + "strong-background-noise/",
            clipsPath = clipsResourcesPath + "strong-background-noise/strong-background-noise.json"
        )
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSignalSubscriberTest") as AudioClipSignalSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Test
    fun generateIncompleteAudioClipsForFileWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        generateAudioClips(
            signalsFolderPath = signalResourcesPath + "background-noise-low-volume-incomplete/",
            clipsPath = clipsResourcesPath + "background-noise-low-volume-incomplete/background-noise-low-volume-incomplete.json"
        )
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSignalSubscriberTest") as AudioClipSignalSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    private suspend fun generateAudioClips(clipsPath: String, signalsFolderPath: String) {
        File(signalsFolderPath).listFiles()
        .filter { it.extension == "json" }
        .forEach { signalFile ->
            val audioSignal = MAPPER.readValue(signalFile, AudioSignalKt::class.java)
            audioSignalDao.storeAudioSignal(audioSignal)
        }
        val clipInfoListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
        val audioClipInfoList: List<AudioClipInfo> = MAPPER.readValue(File(clipsPath), clipInfoListType)
        clipGenerator.generateClips(audioClipsInfo = audioClipInfoList)
    }

}
package net.jcflorezr.broker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.SignalRmsSubscriberConfig
import net.jcflorezr.dao.RedisInitializer
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.model.AudioSignalsRmsInfo
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.Repeat
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [SignalRmsSubscriberConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SignalRmsSubscriberIntegrationTest {

    @Autowired
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioSignalsRmsInfo>

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = RedisInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<SignalRmsSubscriberIntegrationTest> = this.javaClass
    private val testResourcesPath = thisClass.getResource("/signal").path

    @Repeat(value = 1000)
    @Test
    fun generateClipInfoForForAudioWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        generateAudioClipInfo(folderName = "background-noise-low-volume")
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Repeat(value = 1000)
    @Test
    fun generateClipInfoForAudioWithStrongBackgroundNoise() = runBlocking {
        generateAudioClipInfo(folderName = "strong-background-noise")
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Repeat(value = 1000)
    @Test
    fun generateClipInfoForAudioWithApplause() = runBlocking {
        generateAudioClipInfo(folderName = "with-applause")
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    private suspend fun generateAudioClipInfo(folderName: String) = coroutineScope {
        val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfoKt::class.java)
        val audioSignalRmsList: List<AudioSignalRmsInfoKt> =
            MAPPER.readValue(File("$testResourcesPath/$folderName/$folderName.json"), signalRmsListType)
        var start = 0
        var end = 0
        while (end < audioSignalRmsList.size) {
            end = if ((start + 10) >= audioSignalRmsList.size) { audioSignalRmsList.size } else { start + 10 }
            val sublist = audioSignalRmsList.subList(start, end)
            launch(Dispatchers.Default) {
                audioSignalRmsTopic.postMessage(message = AudioSignalsRmsInfo(sublist))
            }
            start = end
        }
    }

}
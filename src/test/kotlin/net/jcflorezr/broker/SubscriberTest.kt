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
        val audioSignalRmsList = getAudioSignalsRmsList(folderName = "background-noise-low-volume")
        generateAudioClipInfo(audioSignalRmsList)
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Repeat(value = 1000)
    @Test
    fun generateClipInfoForAudioWithStrongBackgroundNoise() = runBlocking {
        val audioSignalRmsList = getAudioSignalsRmsList(folderName = "strong-background-noise")
        generateAudioClipInfo(audioSignalRmsList)
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Repeat(value = 1000)
    @Test
    fun generateClipInfoForAudioWithApplause() = runBlocking {
        val audioSignalRmsList = getAudioSignalsRmsList(folderName = "with-applause")
        generateAudioClipInfo(audioSignalRmsList)
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Repeat(value = 30)
    @Test
    fun generateClipInfoForAllAudios() = runBlocking {
        val audioSignalRmsListLowVolume = getAudioSignalsRmsList(folderName = "background-noise-low-volume")
        val audioSignalRmsListStrongNoise = getAudioSignalsRmsList(folderName = "strong-background-noise")
        val audioSignalRmsListApplause = getAudioSignalsRmsList(folderName = "with-applause")
        generateConsolidatedAudioClipInfo(Triple(audioSignalRmsListApplause, audioSignalRmsListLowVolume, audioSignalRmsListStrongNoise))
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    private suspend fun generateAudioClipInfo(audioSignalRmsList: List<AudioSignalRmsInfoKt>) = coroutineScope {
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

    private suspend fun generateConsolidatedAudioClipInfo(
        audioSignalRmsLists: Triple<List<AudioSignalRmsInfoKt>, List<AudioSignalRmsInfoKt>, List<AudioSignalRmsInfoKt>>
    ) = coroutineScope {
        var (start1, start2, start3) = Triple(0, 0, 0)
        var (end1, end2, end3) = Triple(0, 0, 0)
        while (end1 < audioSignalRmsLists.first.size || end2 < audioSignalRmsLists.second.size || end3 < audioSignalRmsLists.third.size) {
            end1 = if ((start1 + 10) >= audioSignalRmsLists.first.size) { audioSignalRmsLists.first.size } else { start1 + 10 }
            val sublist1 = takeIf { end1 <= audioSignalRmsLists.first.size }?.let { audioSignalRmsLists.first.subList(start1, end1) }
            end2 = if ((start2 + 10) >= audioSignalRmsLists.second.size) { audioSignalRmsLists.second.size } else { start2 + 10 }
            val sublist2 = takeIf { end2 <= audioSignalRmsLists.second.size }?.let { audioSignalRmsLists.second.subList(start2, end2) }
            end3 = if ((start3 + 10) >= audioSignalRmsLists.third.size) { audioSignalRmsLists.third.size } else { start3 + 10 }
            val sublist3 = takeIf { end3 <= audioSignalRmsLists.third.size }?.let { audioSignalRmsLists.third.subList(start3, end3) }
            launch(Dispatchers.Default) {
                sublist1?.let { audioSignalRmsTopic.postMessage(message = AudioSignalsRmsInfo(it)) }
                sublist2?.let { audioSignalRmsTopic.postMessage(message = AudioSignalsRmsInfo(it)) }
                sublist3?.let { audioSignalRmsTopic.postMessage(message = AudioSignalsRmsInfo(it)) }
            }
            takeIf { end1 < audioSignalRmsLists.first.size }?.apply { start1 = end1 }
            takeIf { end2 < audioSignalRmsLists.second.size }?.apply { start2 = end2 }
            takeIf { end3 < audioSignalRmsLists.third.size }?.apply { start3 = end3 }
        }
    }

    private fun getAudioSignalsRmsList(folderName: String): List<AudioSignalRmsInfoKt> {
        val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfoKt::class.java)
        return MAPPER.readValue(File("$testResourcesPath/$folderName/$folderName.json"), signalRmsListType)
    }

}
package net.jcflorezr.broker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.AudioClipSubscriberConfig
import net.jcflorezr.config.SignalRmsSubscriberConfig
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.TestRedisInitializer
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.model.AudioSignalsRmsInfo
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
@ContextConfiguration(classes = [SignalRmsSubscriberConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SignalRmsSubscriberIntegrationTest {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioSignalsRmsInfo>

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = TestRedisInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<SignalRmsSubscriberIntegrationTest> = this.javaClass
    private val testResourcesPath = thisClass.getResource("/rms").path

//    @Repeat(value = 50)
    @Test
    fun generateClipInfoForForAudioWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        val audioSignalRmsList = getAudioSignalsRmsList(folderName = "background-noise-low-volume")
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume"))
        generateAudioClipInfo(audioSignalRmsList)
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipInfoSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

//    @Repeat(value = 50)
    @Test
    fun generateClipInfoForAudioWithStrongBackgroundNoise() = runBlocking {
        val audioSignalRmsList = getAudioSignalsRmsList(folderName = "strong-background-noise")
        propsUtils.setTransactionId(sourceAudioFile = File("strong-background-noise"))
        generateAudioClipInfo(audioSignalRmsList)
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipInfoSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

//    @Repeat(value = 50)
    @Test
    fun generateClipInfoForAudioWithApplause() = runBlocking {
        val audioSignalRmsList = getAudioSignalsRmsList(folderName = "with-applause")
        propsUtils.setTransactionId(sourceAudioFile = File("with-applause"))
        generateAudioClipInfo(audioSignalRmsList)
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipInfoSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

//    @Repeat(value = 10)
    @Test
    fun generateClipInfoForAllAudios() = runBlocking {
        val audioSignalRmsListLowVolume = getAudioSignalsRmsList(folderName = "background-noise-low-volume")
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume"))
        val audioSignalRmsListStrongNoise = getAudioSignalsRmsList(folderName = "strong-background-noise")
        propsUtils.setTransactionId(sourceAudioFile = File("strong-background-noise"))
        val audioSignalRmsListApplause = getAudioSignalsRmsList(folderName = "with-applause")
        propsUtils.setTransactionId(sourceAudioFile = File("with-applause"))
        generateConsolidatedAudioClipInfo(
            Triple(
                audioSignalRmsListLowVolume,
                audioSignalRmsListStrongNoise,
                audioSignalRmsListApplause
            )
        )
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipInfoSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    private suspend fun generateAudioClipInfo(audioSignalRmsList: List<AudioSignalRmsInfo>) {
        var start = 0
        var end = 0
        while (end < audioSignalRmsList.size) {
            end = if ((start + 5) >= audioSignalRmsList.size) { audioSignalRmsList.size } else { start + 5 }
            val sublist = audioSignalRmsList.subList(start, end)
            audioSignalRmsTopic.postMessage(message = AudioSignalsRmsInfo(sublist))
            start = end
        }
    }

    private suspend fun generateConsolidatedAudioClipInfo(
        audioSignalRmsLists: Triple<List<AudioSignalRmsInfo>, List<AudioSignalRmsInfo>, List<AudioSignalRmsInfo>>
    ) = coroutineScope {
        var (start1, start2, start3) = Triple(0, 0, 0)
        var (end1, end2, end3) = Triple(0, 0, 0)
        while (end1 < audioSignalRmsLists.first.size || end2 < audioSignalRmsLists.second.size || end3 < audioSignalRmsLists.third.size) {
            end1 = if ((start1 + 5) >= audioSignalRmsLists.first.size) { audioSignalRmsLists.first.size } else { start1 + 5 }
            val sublist1 = takeIf { end1 <= audioSignalRmsLists.first.size }?.let { audioSignalRmsLists.first.subList(start1, end1) }
            end2 = if ((start2 + 5) >= audioSignalRmsLists.second.size) { audioSignalRmsLists.second.size } else { start2 + 5 }
            val sublist2 = takeIf { end2 <= audioSignalRmsLists.second.size }?.let { audioSignalRmsLists.second.subList(start2, end2) }
            end3 = if ((start3 + 5) >= audioSignalRmsLists.third.size) { audioSignalRmsLists.third.size } else { start3 + 5 }
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

    private fun getAudioSignalsRmsList(folderName: String): List<AudioSignalRmsInfo> {
        val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfo::class.java)
        return MAPPER.readValue(File("$testResourcesPath/$folderName/$folderName.json"), signalRmsListType)
    }
}

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [AudioClipSubscriberConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AudioClipSubscriberIntegrationTest {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioClipInfo>
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = TestRedisInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioClipSubscriberIntegrationTest> = this.javaClass
    private val signalsResourcesPath = thisClass.getResource("/signal").path
    private val clipsResourcesPath = thisClass.getResource("/clip").path

//    @Repeat(value = 50)
    @Test
    fun generateClipForForAudioWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        val audioClipInfoList = getAudioClipInfoList(folderName = "background-noise-low-volume")
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume"))
        generateAudioSignalsForClips(folderName = "background-noise-low-volume")
        generateAudioClipInfo(audioClipInfoList)
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberMockTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

//    @Repeat(value = 50)
    @Test
    fun generateClipInfoForAudioWithStrongBackgroundNoise() = runBlocking {
        val audioClipInfoList = getAudioClipInfoList(folderName = "strong-background-noise")
        propsUtils.setTransactionId(sourceAudioFile = File("strong-background-noise"))
        generateAudioSignalsForClips(folderName = "strong-background-noise")
        generateAudioClipInfo(audioClipInfoList)
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberMockTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

//    @Repeat(value = 50)
    @Test
    fun generateClipInfoForAudioWithApplause() = runBlocking {
        val audioClipInfoList = getAudioClipInfoList(folderName = "with-applause")
        propsUtils.setTransactionId(sourceAudioFile = File("with-applause"))
        generateAudioSignalsForClips(folderName = "with-applause")
        generateAudioClipInfo(audioClipInfoList)
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberMockTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

//    @Repeat(value = 50)
    @Test
    fun generateIncompleteClipForForAudioWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        val audioClipInfoList = getAudioClipInfoList(folderName = "background-noise-low-volume-incomplete")
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume-incomplete"))
        generateAudioSignalsForClips(folderName = "background-noise-low-volume-incomplete")
        generateAudioClipInfo(audioClipInfoList)
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberMockTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

//    @Repeat(value = 10)
    @Test
    fun generateClipForAllAudios() = runBlocking {
        val audioClipsListLowVolume = getAudioClipInfoList(folderName = "background-noise-low-volume")
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume"))
        val audioClipsListStrongNoise = getAudioClipInfoList(folderName = "strong-background-noise")
        propsUtils.setTransactionId(sourceAudioFile = File("strong-background-noise"))
        val audioClipsListApplause = getAudioClipInfoList(folderName = "with-applause")
        propsUtils.setTransactionId(sourceAudioFile = File("with-applause"))
        val audioClipsListIncompleteLowVolume = getAudioClipInfoList(folderName = "background-noise-low-volume-incomplete")
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume-incomplete"))
        generateAudioSignalsForClips(folderName = "background-noise-low-volume")
        generateAudioSignalsForClips(folderName = "strong-background-noise")
        generateAudioSignalsForClips(folderName = "with-applause")
        generateConsolidatedAudioClipInfo(
            listOf(
                audioClipsListApplause,
                audioClipsListLowVolume,
                audioClipsListStrongNoise,
                audioClipsListIncompleteLowVolume
            )
        )
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberMockTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

    private suspend fun generateAudioClipInfo(audioClipInfoList: List<AudioClipInfo>) {
        var start = 0
        var end = 0
        while (end < audioClipInfoList.size) {
            end = if ((start + 2) >= audioClipInfoList.size) {
                audioClipInfoList.size
            } else {
                start + 2
            }
            val sublist = audioClipInfoList.subList(start, end)
            sublist.forEach { audioSignalRmsTopic.postMessage(message = it) }
            start = end
        }
    }

    private suspend fun generateConsolidatedAudioClipInfo(
        audioClipInfoLists: List<List<AudioClipInfo>>
    ) = coroutineScope {
        var (start1, start2, start3, start4) = listOf(0, 0, 0, 0)
        var (end1, end2, end3, end4) = listOf(0, 0, 0, 0)
        while (end1 < audioClipInfoLists[0].size || end2 < audioClipInfoLists[1].size ||
                end3 < audioClipInfoLists[2].size || end4 < audioClipInfoLists[3].size) {
            end1 = if ((start1 + 2) >= audioClipInfoLists[0].size) { audioClipInfoLists[0].size } else { start1 + 3 }
            val sublist1 = takeIf { end1 <= audioClipInfoLists[0].size }?.let { audioClipInfoLists[0].subList(start1, end1) }
            end2 = if ((start2 + 2) >= audioClipInfoLists[1].size) { audioClipInfoLists[1].size } else { start2 + 3 }
            val sublist2 = takeIf { end2 <= audioClipInfoLists[1].size }?.let { audioClipInfoLists[1].subList(start2, end2) }
            end3 = if ((start3 + 2) >= audioClipInfoLists[2].size) { audioClipInfoLists[2].size } else { start3 + 3 }
            val sublist3 = takeIf { end3 <= audioClipInfoLists[2].size }?.let { audioClipInfoLists[2].subList(start3, end3) }
            end4 = if ((start4 + 2) >= audioClipInfoLists[3].size) { audioClipInfoLists[3].size } else { start4 + 3 }
            val sublist4 = takeIf { end4 <= audioClipInfoLists[3].size }?.let { audioClipInfoLists[3].subList(start4, end4) }
            sublist1?.forEach { audioSignalRmsTopic.postMessage(message = it) }
            sublist2?.forEach { audioSignalRmsTopic.postMessage(message = it) }
            sublist3?.forEach { audioSignalRmsTopic.postMessage(message = it) }
            sublist4?.forEach { audioSignalRmsTopic.postMessage(message = it) }
            takeIf { end1 < audioClipInfoLists[0].size }?.apply { start1 = end1 }
            takeIf { end2 < audioClipInfoLists[1].size }?.apply { start2 = end2 }
            takeIf { end3 < audioClipInfoLists[2].size }?.apply { start3 = end3 }
            takeIf { end4 < audioClipInfoLists[3].size }?.apply { start4 = end4 }
        }
    }

    private fun generateAudioSignalsForClips(folderName: String) {
        File("$signalsResourcesPath/$folderName").listFiles()
            .filter { it.extension == "json" }
            .forEach { signalFile ->
                val audioSignal = MAPPER.readValue(signalFile, AudioSignal::class.java)
                audioSignalDao.storeAudioSignal(audioSignal)
            }
    }

    private fun getAudioClipInfoList(folderName: String): List<AudioClipInfo> {
        val clipInfoListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
        return MAPPER.readValue(File("$clipsResourcesPath/$folderName/$folderName.json"), clipInfoListType)
    }
}
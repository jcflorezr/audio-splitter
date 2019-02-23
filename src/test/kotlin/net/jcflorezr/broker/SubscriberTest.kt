package net.jcflorezr.broker

import biz.source_code.dsp.model.AudioSignalKt
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.AudioClipSubscriberConfig
import net.jcflorezr.config.SignalRmsSubscriberConfig
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.RedisInitializer
import net.jcflorezr.model.AudioClipInfo
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
    private val testResourcesPath = thisClass.getResource("/rms").path

    @Repeat(value = 1000)
    @Test
    fun generateClipInfoForForAudioWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        val audioSignalRmsList = getAudioSignalsRmsList(folderName = "background-noise-low-volume")
        generateAudioClipInfo(audioSignalRmsList)
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipInfoSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Repeat(value = 1000)
    @Test
    fun generateClipInfoForAudioWithStrongBackgroundNoise() = runBlocking {
        val audioSignalRmsList = getAudioSignalsRmsList(folderName = "strong-background-noise")
        generateAudioClipInfo(audioSignalRmsList)
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipInfoSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Repeat(value = 1000)
    @Test
    fun generateClipInfoForAudioWithApplause() = runBlocking {
        val audioSignalRmsList = getAudioSignalsRmsList(folderName = "with-applause")
        generateAudioClipInfo(audioSignalRmsList)
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipInfoSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    // TODO: this test fails when querying for remaining processed rms in db amd I do not have any idea why
    @Repeat(value = 30)
    @Test
    fun generateClipInfoForAllAudios() = runBlocking {
        val audioSignalRmsListLowVolume = getAudioSignalsRmsList(folderName = "background-noise-low-volume")
        val audioSignalRmsListStrongNoise = getAudioSignalsRmsList(folderName = "strong-background-noise")
        val audioSignalRmsListApplause = getAudioSignalsRmsList(folderName = "with-applause")
        generateConsolidatedAudioClipInfo(Triple(audioSignalRmsListApplause, audioSignalRmsListLowVolume, audioSignalRmsListStrongNoise))
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSubscriberMockSubscriberTest") as AudioClipInfoSubscriberMock
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


@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [AudioClipSubscriberConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AudioClipSubscriberIntegrationTest {

    @Autowired
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioClipInfo>
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = RedisInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioClipSubscriberIntegrationTest> = this.javaClass
    private val signalsResourcesPath = thisClass.getResource("/signal").path
    private val clipsResourcesPath = thisClass.getResource("/clip").path

    @Repeat(value = 50)
    @Test
    fun generateClipForForAudioWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        val audioClipInfoList = getAudioClipInfoList(folderName = "background-noise-low-volume")
        generateAudioSignalsForClips(folderName = "background-noise-low-volume")
        generateAudioClipInfo(audioClipInfoList)
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberMockTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

    @Repeat(value = 50)
    @Test
    fun generateClipInfoForAudioWithStrongBackgroundNoise() = runBlocking {
        val audioClipInfoList = getAudioClipInfoList(folderName = "strong-background-noise")
        generateAudioSignalsForClips(folderName = "strong-background-noise")
        generateAudioClipInfo(audioClipInfoList)
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberMockTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

    @Repeat(value = 50)
    @Test
    fun generateClipInfoForAudioWithApplause() = runBlocking {
        val audioClipInfoList = getAudioClipInfoList(folderName = "with-applause")
        generateAudioSignalsForClips(folderName = "with-applause")
        generateAudioClipInfo(audioClipInfoList)
        val audioClipSignalSubscriber = applicationCtx.getBean("audioClipSignalSubscriberMockTest") as AudioClipSignalSubscriberMock
        audioClipSignalSubscriber.validateCompleteness()
    }

    @Repeat(value = 5)
    @Test
    fun generateClipForAllAudios() = runBlocking {
        val audioClipsListLowVolume = getAudioClipInfoList(folderName = "background-noise-low-volume")
        val audioClipsListStrongNoise = getAudioClipInfoList(folderName = "strong-background-noise")
        val audioClipsListApplause = getAudioClipInfoList(folderName = "with-applause")
        generateAudioSignalsForClips(folderName = "background-noise-low-volume")
        generateAudioSignalsForClips(folderName = "strong-background-noise")
        generateAudioSignalsForClips(folderName = "with-applause")
        generateConsolidatedAudioClipInfo(Triple(audioClipsListApplause, audioClipsListLowVolume, audioClipsListStrongNoise))
        val signalRmsSubscriber = applicationCtx.getBean("audioClipSignalSubscriberMockTest") as AudioClipSignalSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    private suspend fun generateAudioClipInfo(audioClipInfoList: List<AudioClipInfo>) = coroutineScope {
        var start = 0
        var end = 0
        while (end < audioClipInfoList.size) {
            end = if ((start + 2) >= audioClipInfoList.size) {
                audioClipInfoList.size
            } else {
                start + 2
            }
            val sublist = audioClipInfoList.subList(start, end)
            sublist.forEach {
                launch(Dispatchers.Default) {
                    audioSignalRmsTopic.postMessage(message = it)
                }
            }
            start = end
        }
    }

    private suspend fun generateConsolidatedAudioClipInfo(
        audioClipInfoLists: Triple<List<AudioClipInfo>, List<AudioClipInfo>, List<AudioClipInfo>>
    ) = coroutineScope {
        var (start1, start2, start3) = Triple(0, 0, 0)
        var (end1, end2, end3) = Triple(0, 0, 0)
        while (end1 < audioClipInfoLists.first.size || end2 < audioClipInfoLists.second.size || end3 < audioClipInfoLists.third.size) {
            end1 = if ((start1 + 2) >= audioClipInfoLists.first.size) { audioClipInfoLists.first.size } else { start1 + 3 }
            val sublist1 = takeIf { end1 <= audioClipInfoLists.first.size }?.let { audioClipInfoLists.first.subList(start1, end1) }
            end2 = if ((start2 + 2) >= audioClipInfoLists.second.size) { audioClipInfoLists.second.size } else { start2 + 3 }
            val sublist2 = takeIf { end2 <= audioClipInfoLists.second.size }?.let { audioClipInfoLists.second.subList(start2, end2) }
            end3 = if ((start3 + 2) >= audioClipInfoLists.third.size) { audioClipInfoLists.third.size } else { start3 + 3 }
            val sublist3 = takeIf { end3 <= audioClipInfoLists.third.size }?.let { audioClipInfoLists.third.subList(start3, end3) }
            sublist1?.forEach { launch(Dispatchers.Default) {audioSignalRmsTopic.postMessage(message = it) } }
            sublist2?.forEach { launch(Dispatchers.Default) { audioSignalRmsTopic.postMessage(message = it) } }
            sublist3?.forEach { launch(Dispatchers.Default) { audioSignalRmsTopic.postMessage(message = it) } }
            takeIf { end1 < audioClipInfoLists.first.size }?.apply { start1 = end1 }
            takeIf { end2 < audioClipInfoLists.second.size }?.apply { start2 = end2 }
            takeIf { end3 < audioClipInfoLists.third.size }?.apply { start3 = end3 }
        }
    }

    private fun generateAudioSignalsForClips(folderName: String) {
        File("$signalsResourcesPath/$folderName").listFiles()
            .filter { it.extension == "json" }
            .forEach { signalFile ->
                val audioSignal = MAPPER.readValue(signalFile, AudioSignalKt::class.java)
                audioSignalDao.storeAudioSignal(audioSignal)
            }
    }

    private fun getAudioClipInfoList(folderName: String): List<AudioClipInfo> {
        val clipInfoListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
        return MAPPER.readValue(File("$clipsResourcesPath/$folderName/$folderName.json"), clipInfoListType)
    }
}
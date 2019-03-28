package net.jcflorezr.broker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.util.PropsUtils
import org.apache.commons.io.FilenameUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import javax.annotation.PostConstruct
import org.hamcrest.CoreMatchers.`is` as Is

@Service
final class SourceFileSubscriberMock : Subscriber<InitialConfiguration> {

    @Autowired
    private lateinit var sourceFileTopic: Topic<InitialConfiguration>

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val logger = KotlinLogging.logger { }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<SourceFileSubscriberMock>

    @PostConstruct
    fun init() {
        sourceFileTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/entrypoint").path
    }

    override suspend fun update(message: InitialConfiguration) {
        val transactionId = PropsUtils.getTransactionId(message.audioFileMetadata!!.audioFileName)
        val expectedConfiguration = MAPPER.readValue<InitialConfiguration>(File("$testResourcesPath/initial-configuration.json"))
        logger.info { "[$transactionId][TEST] testing initial configuration ==> $message" }
        assertTrue(File(message.audioFileLocation).exists())
        assertThat(FilenameUtils.getName(message.convertedAudioFileLocation), Is(equalTo(expectedConfiguration.convertedAudioFileLocation)))
        assertThat(message.audioFileMetadata.toString(), Is(equalTo(expectedConfiguration.audioFileMetadata.toString())))
    }

}

@Service
final class SignalSubscriberMock : Subscriber<AudioSignal> {

    @Autowired
    private lateinit var signalTopic: Topic<AudioSignal>

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val logger = KotlinLogging.logger { }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<SignalSubscriberMock>

    @PostConstruct
    fun init() {
        signalTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/signal").path
    }

    override suspend fun update(message: AudioSignal) {
        val transactionId = PropsUtils.getTransactionId(message.audioFileName)
        val folderName = FilenameUtils.getBaseName(message.audioFileName)
        val fileNamePrefix = (message.index).toString().replace(".", "_")
        val signalPartFilePath = "$testResourcesPath/$folderName/$fileNamePrefix-$folderName.json"
        val signalPart = File(signalPartFilePath)
            .takeIf { it.exists() }
            ?.let { signalJsonFile -> MAPPER.readValue<AudioSignal>(signalJsonFile) }
        logger.info { "[$transactionId][TEST] testing audio rms ==> audioFileName: ${message.audioFileName} - " +
            "index: ${message.index} - initialPositionInSeconds: ${message.initialPositionInSeconds}" }
        assertNotNull("No rms part was found for ----> $message", signalPart)
        assertThat("Current and expected rms parts are not equal \n " +
            "Current: $message \n Expected: $signalPart", message, Is(equalTo(signalPart)))
    }

}

@Service
final class SignalRmsSubscriberMock : Subscriber<AudioSignalsRmsInfo> {

    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioSignalsRmsInfo>

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val logger = KotlinLogging.logger { }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<SignalRmsSubscriberMock>
    private var audioSignalRmsList: ArrayList<AudioSignalRmsInfo> = ArrayList()

    @PostConstruct
    fun init() {
        audioSignalRmsTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/rms").path
    }

    override suspend fun update(message: AudioSignalsRmsInfo) {
        val transactionId = PropsUtils.getTransactionId(message.audioSignals.first().audioFileName)
        val folderName = FilenameUtils.getBaseName(message.audioSignals.first().audioFileName)
        if (audioSignalRmsList.isEmpty()) {
            val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfo::class.java)
            audioSignalRmsList = MAPPER.readValue(File("$testResourcesPath/$folderName/$folderName.json"), signalRmsListType)
        }
        message.audioSignals.forEach {
            logger.info { "[$transactionId][TEST] testing audio rms ==> audioFileName: ${it.audioFileName} - " +
                "index: ${it.index} - initialPositionInSeconds: ${it.initialPositionInSeconds} - segmentSize: ${it.segmentSize}" }
            assertTrue("signalRms ----> $it  was not found", audioSignalRmsList.contains(it))
            audioSignalRmsList.remove(it)
        }
    }

    fun validateCompleteness() {
        assertTrue("There were ${audioSignalRmsList.size} signals rms that were not tested -----> $audioSignalRmsList",
            audioSignalRmsList.isEmpty())
    }

}

@Service
final class AudioClipInfoSubscriberMock : Subscriber<AudioClipInfo> {

    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val logger = KotlinLogging.logger { }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<AudioClipInfoSubscriberMock>
    private lateinit var audioFileName: String
    private val expectedClipInfoList: ArrayList<AudioClipInfo> = ArrayList()
    private val unexpectedClipInfoList: ArrayList<AudioClipInfo> = ArrayList()
    private val actualClipInfoList: ArrayList<AudioClipInfo> = ArrayList()
    private val foldersProcessed: HashSet<String> = HashSet()

    @PostConstruct
    fun init() {
        audioClipTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/clip").path
    }

    override suspend fun update(message: AudioClipInfo) {
        val folderName = FilenameUtils.getBaseName(message.audioFileName)
        val transactionId = PropsUtils.getTransactionId(message.audioFileName)
        if (expectedClipInfoList.isEmpty() || !foldersProcessed.contains(folderName)) {
            val previousExpectedClipsListSize = expectedClipInfoList.size
            val audioClipListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
            expectedClipInfoList.addAll(MAPPER.readValue(File("$testResourcesPath/$folderName/$folderName.json"), audioClipListType))
            foldersProcessed.add(folderName)
            audioFileName = message.audioFileName
            logger.info { "[$transactionId][TEST] List of expected clips info was created for $folderName. " +
                "Num of new expected clips: ${Math.abs(expectedClipInfoList.size - previousExpectedClipsListSize)}" }
        }
        logger.info { "[$transactionId][TEST] testing clip info ==> last clip: ${message.lastClip} - consecutive: ${message.consecutive} - " +
            "clip name: ${message.audioClipName} - length: (from ${message.initialPositionInSeconds} to ${message.endPositionInSeconds})" }
        if (!expectedClipInfoList.contains(message)) {
            unexpectedClipInfoList.add(message)
            logger.error { "[$transactionId][TEST] unexpected clip was generated ==> $message" }
        } else {
            actualClipInfoList.add(message)
            logger.info { "[$transactionId][TEST] expected clip was generated ==> $message" }
        }
    }

    suspend fun validateCompleteness() {
        delay(1000L) // giving some time until lists are fully populated
        assertTrue(getUnexpectedClipsErrorMessage(), unexpectedClipInfoList.isEmpty())
        assertThat(getMissingExpectedClipsErrorMessage(), actualClipInfoList.size, Is(equalTo(expectedClipInfoList.size)))
    }

    private fun getUnexpectedClipsErrorMessage(): String {
        val errorMessage = "There were ${unexpectedClipInfoList.size} unexpected clips. \n"
        return errorMessage + unexpectedClipInfoList.map { "$it \n" }
    }

    private fun getMissingExpectedClipsErrorMessage(): String {
        val actualNumOfClips = actualClipInfoList.size
        val expectedNumOfClips = expectedClipInfoList.size
        val errorMessage = "There were ${expectedNumOfClips - actualNumOfClips} num of clips that were not tested. \n"
        return errorMessage + expectedClipInfoList.filter { !actualClipInfoList.contains(it) }.map { "$it \n" }
    }

}

@Service
final class AudioClipSignalSubscriberMock : Subscriber<AudioClipSignal> {

    @Autowired
    private lateinit var audioClipSignalTopic: Topic<AudioClipSignal>
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao
    @Autowired
    private lateinit var audioClipDao: AudioClipDao

    private val logger = KotlinLogging.logger { }

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private lateinit var thisClass: Class<AudioClipSignalSubscriberMock>
    private lateinit var testResourcesPath: String
    private lateinit var audioFileName: String
    private var expectedAudioClipSignalList: ArrayList<AudioClipSignal> = ArrayList()
    private val unexpectedClipSignalList: ArrayList<AudioClipSignal> = ArrayList()
    private val actualClipSignalList: ArrayList<AudioClipSignal> = ArrayList()
    private val foldersProcessed: HashSet<String> = HashSet()

    @PostConstruct
    fun init() {
        audioClipSignalTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/clip").path
    }

    override suspend fun update(message: AudioClipSignal) {
        val folderName = FilenameUtils.getBaseName(message.audioFileName)
        val transactionId = PropsUtils.getTransactionId(message.audioFileName)
        if (expectedAudioClipSignalList.isEmpty() || !foldersProcessed.contains(folderName)) {
            val previousExpectedClipsListSize = expectedAudioClipSignalList.size
            expectedAudioClipSignalList.addAll(File("$testResourcesPath/$folderName/signal/").listFiles()
                .map { MAPPER.readValue(it, AudioClipSignal::class.java) } as ArrayList<AudioClipSignal>)
            foldersProcessed.add(folderName)
            audioFileName = message.audioFileName
            logger.info { "[$transactionId][TEST] List of expected clips signals was created for $folderName. " +
                "Num of new expected clips: ${Math.abs(expectedAudioClipSignalList.size - previousExpectedClipsListSize)}" }
        }
        logger.info { "[$transactionId][TEST] testing clip signal ==> clip name: ${message.audioClipName}" }
        assertTrue("audio clip signal ----> $message was not found", expectedAudioClipSignalList.contains(message))
        if (!expectedAudioClipSignalList.contains(message)) {
            unexpectedClipSignalList.add(message)
            logger.error { "[$transactionId][TEST] unexpected clip was generated ==> $message" }
        } else {
            actualClipSignalList.add(message)
            logger.info { "[$transactionId][TEST] expected clip was generated ==> $message" }
        }
    }

    suspend fun validateCompleteness() {
        delay(1000L) // giving some time until lists are fully populated
        assertTrue(getUnexpectedClipsErrorMessage(), unexpectedClipSignalList.isEmpty())
        assertThat(getMissingExpectedClipsErrorMessage(), actualClipSignalList.size, Is(equalTo(expectedAudioClipSignalList.size)))
        val signalsList = audioSignalDao.retrieveAllAudioSignals(key = "audioSignal_$audioFileName")
        assertTrue("There were ${signalsList.size} audio signals that were not removed from database ----> $signalsList",
            signalsList.isEmpty())
        val audioClipInfoList = audioClipDao.retrieveAllAudioClipsInfo(key = "audioClipInfo_$audioFileName")
        assertTrue("There were ${audioClipInfoList.size} audio clips info that were not removed from database ----> $audioClipInfoList",
            audioClipInfoList.isEmpty())
    }

    private fun getUnexpectedClipsErrorMessage(): String {
        val errorMessage = "There were ${unexpectedClipSignalList.size} unexpected clips. \n"
        return errorMessage + unexpectedClipSignalList.map { "$it \n" }
    }

    private fun getMissingExpectedClipsErrorMessage(): String {
        val actualNumOfClips = actualClipSignalList.size
        val expectedNumOfClips = expectedAudioClipSignalList.size
        val errorMessage = "There were ${expectedNumOfClips - actualNumOfClips} num of clips that were not tested. \n"
        return errorMessage + expectedAudioClipSignalList.filter { !actualClipSignalList.contains(it) }.map { "$it \n" }
    }

}
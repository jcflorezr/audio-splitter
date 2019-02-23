package net.jcflorezr.broker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.model.AudioSignalKt
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.model.InitialConfiguration
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

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<SourceFileSubscriberMock>

    @PostConstruct
    fun init() {
        sourceFileTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/entrypoint").path
    }

    override suspend fun update(message: InitialConfiguration) {
        val expectedConfiguration = MAPPER.readValue<InitialConfiguration>(File("$testResourcesPath/initial-configuration.json"))
        // TODO: implement a logger
        println("testing initial configuration ----> $message")
        assertTrue(File(message.audioFileLocation).exists())
        assertThat(message.convertedAudioFileLocation, Is(equalTo(expectedConfiguration.convertedAudioFileLocation)))
        assertThat(message.audioClipsAsStereo, Is(equalTo(expectedConfiguration.audioClipsAsStereo)))
        assertThat(message.audioClipsByGroup, Is(equalTo(expectedConfiguration.audioClipsByGroup)))
        assertThat(message.audioFileMetadata.toString(), Is(equalTo(expectedConfiguration.audioFileMetadata.toString())))
    }

}

@Service
final class SignalSubscriberMock : Subscriber<AudioSignalKt> {

    @Autowired
    private lateinit var signalTopic: Topic<AudioSignalKt>

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<SignalSubscriberMock>

    @PostConstruct
    fun init() {
        signalTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/signal").path
    }

    override suspend fun update(message: AudioSignalKt) {
        val folderName = FilenameUtils.getBaseName(message.audioFileName)
        val fileNamePrefix = (message.index).toString().replace(".", "_")
        val signalPartFilePath = "$testResourcesPath/$folderName/$fileNamePrefix-$folderName.json"
        val signalPart = File(signalPartFilePath)
            .takeIf { it.exists() }
            ?.let { signalJsonFile -> MAPPER.readValue<AudioSignalKt>(signalJsonFile) }
        // TODO: implement a logger
        println("testing audio rms -----> " +
            "{audioFileName: ${message.audioFileName}, " +
            "index: ${message.index}, " +
            "initialPositionInSeconds: ${message.initialPositionInSeconds}}")
        assertNotNull("No rms part was found for ----> $message", signalPart)
        assertThat("Current and expected rms parts are not equal \n " +
            "Current: $message \n Expected: $signalPart", message, Is(equalTo(signalPart)))
    }

    // TODO: is it possible to implement a validateCompleteness() fun as below tests?

}

@Service
final class SignalRmsSubscriberMock : Subscriber<AudioSignalsRmsInfo> {

    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioSignalsRmsInfo>
    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<SignalRmsSubscriberMock>
    private var audioSignalRmsList: ArrayList<AudioSignalRmsInfoKt> = ArrayList()

    @PostConstruct
    fun init() {
        audioSignalRmsTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/rms").path
    }

    override suspend fun update(message: AudioSignalsRmsInfo) {
        val folderName = FilenameUtils.getBaseName(message.audioSignals.first().audioFileName)
        if (audioSignalRmsList.isEmpty()) {
            val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfoKt::class.java)
            audioSignalRmsList = MAPPER.readValue(File("$testResourcesPath/$folderName/$folderName.json"), signalRmsListType)
        }
        // TODO: implement a logger
        message.audioSignals.forEach {
            println("testing audio rms ---> " +
                "{audioFileName: ${it.audioFileName}, " +
                "index: ${it.index}, " +
                "initialPositionInSeconds: ${it.initialPositionInSeconds}, " +
                "segmentSize: ${it.segmentSize}}")
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

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<AudioClipInfoSubscriberMock>
    private lateinit var audioFileName: String
    private var audioClipList: ArrayList<AudioClipInfo> = ArrayList()
    private val foldersProcessed: HashSet<String> = HashSet()

    @PostConstruct
    fun init() {
        audioClipTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/clip").path
    }

    override suspend fun update(message: AudioClipInfo) = runBlocking<Unit> {
        val folderName = FilenameUtils.getBaseName(message.audioFileName)
        if (audioClipList.isEmpty() || !foldersProcessed.contains(folderName)) {
            val audioClipListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
            audioClipList.addAll(MAPPER.readValue(File("$testResourcesPath/$folderName/$folderName.json"), audioClipListType))
            foldersProcessed.add(folderName)
            audioFileName = message.audioFileName
        }
        // TODO: implement a logger
        println("testing audio clip ----> " +
            "{audioFileName: ${message.audioFileName}, " +
            "initialPositionInSeconds: ${message.initialPositionInSeconds}, " +
            "endPositionInSeconds: ${message.endPositionInSeconds}}")
        assertTrue("audio clip ----> $message was not found", audioClipList.contains(message))
        audioClipList.remove(message)
    }

    fun validateCompleteness() {
        assertTrue("There were ${audioClipList.size} audio clips that were not tested ----> $audioClipList",
            audioClipList.isEmpty())
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

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private lateinit var thisClass: Class<AudioClipSignalSubscriberMock>
    private lateinit var testResourcesPath: String
    private lateinit var audioFileName: String
    private var audioClipSignalList = ArrayList<AudioClipSignal>()
    private val foldersProcessed: HashSet<String> = HashSet()

    @PostConstruct
    fun init() {
        audioClipSignalTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/clip").path
    }

    override suspend fun update(message: AudioClipSignal) {
        val folderName = FilenameUtils.getBaseName(message.audioFileName)
        if (audioClipSignalList.isEmpty() || !foldersProcessed.contains(folderName)) {
            audioClipSignalList.addAll(File("$testResourcesPath/$folderName/signal/").listFiles()
                .map { MAPPER.readValue(it, AudioClipSignal::class.java) } as ArrayList<AudioClipSignal>)
            foldersProcessed.add(folderName)
            audioFileName = message.audioFileName
        }
        // TODO: implement a logger
        println("testing audio clip signal ----> {audioFileName: ${message.audioFileName}, audioClipName: ${message.audioClipName}}")
        assertTrue("audio clip signal ----> $message was not found", audioClipSignalList.contains(message))
        audioClipSignalList.remove(message)
    }

    fun validateCompleteness() {
        assertTrue("There were ${audioClipSignalList.size} audio signal clips that were not tested ----> $audioClipSignalList",
            audioClipSignalList.isEmpty())
        val signalsList = audioSignalDao.retrieveAllAudioSignals(key = "audioSignal_$audioFileName")
        assertTrue("There were ${signalsList.size} audio signals that were not removed from database ----> $signalsList",
            signalsList.isEmpty())
        val audioClipInfoList = audioClipDao.retrieveAllAudioClipsInfo(key = "audioClipInfo_$audioFileName")
        assertTrue("There were ${audioClipInfoList.size} audio clips info that were not removed from database ----> $audioClipInfoList",
            audioClipInfoList.isEmpty())
    }

}
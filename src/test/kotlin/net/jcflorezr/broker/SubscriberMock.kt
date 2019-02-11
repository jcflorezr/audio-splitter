package net.jcflorezr.broker

import biz.source_code.dsp.model.AudioSignalKt
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsEntity
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
        testResourcesPath = thisClass.getResource("/source").path
    }

    override suspend fun update(message: InitialConfiguration) {
        val expectedConfiguration = MAPPER.readValue<InitialConfiguration>(File("$testResourcesPath/initial-configuration.json"))
        // TODO: implement a logger
        println("testing initial configuration ----> $message")
        assertThat(message.toString(), Is(equalTo(expectedConfiguration.toString())))
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
        testResourcesPath = thisClass.getResource("/sound").path
    }

    override suspend fun update(message: AudioSignalKt) {
        val folderName = FilenameUtils.getBaseName(message.audioFileName)
        val signalPartFilePath = "$testResourcesPath/$folderName/${message.index}-$folderName.json"
        val signalPart = File(signalPartFilePath)
            .takeIf { it.exists() }
            ?.let { signalJsonFile -> MAPPER.readValue<AudioSignalKt>(signalJsonFile) }
        // TODO: implement a logger
        println("testing audio signal -----> " +
            "{audioFileName: ${message.audioFileName}, " +
            "index: ${message.index}, " +
            "initialPositionInSeconds: ${message.initialPositionInSeconds}}")
        assertNotNull("No signal part was found for ----> $message", signalPart)
        assertThat("Current and expected signal parts are not equal \n " +
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
        testResourcesPath = thisClass.getResource("/signal").path
    }

    override suspend fun update(message: AudioSignalsRmsInfo) {
        val folderName = FilenameUtils.getBaseName(message.audioSignals.first().audioFileName)
        if (audioSignalRmsList.isEmpty()) {
            val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfoKt::class.java)
            audioSignalRmsList = MAPPER.readValue(File("$testResourcesPath/$folderName/$folderName.json"), signalRmsListType)
        }
        // TODO: implement a logger
        message.audioSignals.forEach {
            println("testing audio signal rms ---> " +
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
final class AudioClipSubscriberMock : Subscriber<AudioClipInfo> {

    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<AudioClipSubscriberMock>
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
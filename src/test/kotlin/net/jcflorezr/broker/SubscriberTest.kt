package net.jcflorezr.broker

import biz.source_code.dsp.model.AudioSignalKt
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.model.AudioSignalRmsInfoKt
import org.apache.commons.io.FilenameUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import javax.annotation.PostConstruct
import org.hamcrest.CoreMatchers.`is` as Is

@Service
final class SourceFileSubscriberTest : Subscriber {

    @Autowired
    private lateinit var sourceFileTopic: Topic<InitialConfiguration>

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<SourceFileSubscriberTest>

    @PostConstruct
    fun init() {
        sourceFileTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/source/").path
    }

    override fun update() {
        val expectedConfiguration =
            MAPPER.readValue<InitialConfiguration>(File(testResourcesPath + "initial-configuration.json"))
        println("testing initial configuration...")
        val initialConfiguration = sourceFileTopic.getMessage()
        assertThat(initialConfiguration.toString(), Is(equalTo(expectedConfiguration.toString())))
    }

}

@Service
final class SignalSubscriberTest : Subscriber {

    @Autowired
    private lateinit var signalTopic: Topic<AudioSignalKt>

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<SignalSubscriberTest>

    @PostConstruct
    fun init() {
        signalTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/sound/").path
    }

    override fun update() {
        val audioSignal = signalTopic.getMessage()
        val folderName = FilenameUtils.getBaseName(audioSignal.audioFileName)
        val path = "$testResourcesPath$folderName/"
        val signalParts = File(path).listFiles()
            .filter { it.extension == "json" }
            .map { signalJsonFile -> MAPPER.readValue<AudioSignalKt>(signalJsonFile) }
        println("check if current signal part exists")
        Assert.assertTrue(signalParts.contains(audioSignal))
    }

}

@Service
final class SignalRmsSubscriberTest : Subscriber {

    @Autowired
    private lateinit var audioSignalRmsTopic: Topic<AudioSignalRmsInfoKt>

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<SignalRmsSubscriberTest>

    @PostConstruct
    fun init() {
        audioSignalRmsTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/signal/").path
    }

    override fun update() {
        val signalRms = audioSignalRmsTopic.getMessage()
        // TODO: implement a logger
        println("check if current signal rms exists")
        val folderName = FilenameUtils.getBaseName(signalRms.audioFileName)
        val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfoKt::class.java)
        val audioSignalRmsList: List<AudioSignalRmsInfoKt> =
            MAPPER.readValue(File("$testResourcesPath$folderName/$folderName.json"), signalRmsListType)
        Assert.assertTrue(audioSignalRmsList.contains(signalRms))
    }

}

@Service
final class AudioClipSubscriberTest : Subscriber {

    @Autowired
    private lateinit var audioClipTopic: Topic<AudioClipInfo>

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private lateinit var testResourcesPath: String
    private lateinit var thisClass: Class<AudioClipSubscriberTest>

    @PostConstruct
    fun init() {
        audioClipTopic.register(this)
        thisClass = this.javaClass
        testResourcesPath = thisClass.getResource("/clip/").path
    }

    override fun update() {
        val audioClip = audioClipTopic.getMessage()
        println("check if current audio clip exists")
        val folderName = FilenameUtils.getBaseName(audioClip.audioFileName)
        val audioClipListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
        val audioClipList: List<AudioClipInfo> =
            MAPPER.readValue(File("$testResourcesPath$folderName/$folderName.json"), audioClipListType)
        Assert.assertTrue(audioClipList.contains(audioClip))
    }

}
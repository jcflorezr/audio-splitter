package net.jcflorezr.dao

import biz.source_code.dsp.model.AudioSignalKt
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.config.SignalDaoConfig
import net.jcflorezr.config.SignalRmsDaoConfig
import net.jcflorezr.model.AudioPartEntity
import net.jcflorezr.model.AudioSignalRmsInfoKt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import org.hamcrest.CoreMatchers.`is` as Is

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [SignalDaoConfig::class])
class AudioSignalDaoIntegrationTest {

    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    private val thisClass: Class<AudioSignalDaoIntegrationTest> = this.javaClass
    private val testSignalResourcesPath: String = thisClass.getResource("/sound").path

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = RedisInitializer()
        @JvmField
        @ClassRule
        val cassandraInitializer = CassandraInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
        private const val AUDIO_PART_TABLE = "AUDIO_PART"
    }

    @Test
    fun storePartForAudioWithBackgroundNoiseAndLowVoiceVolume() {
        storePart(sourceFilesFolderPath = "$testSignalResourcesPath/strong-background-noise/")
    }

    @Test
    fun storePartForAudioWithStrongBackgroundNoise() {
        storePart(sourceFilesFolderPath = "$testSignalResourcesPath/background-noise-low-volume/")
    }

    @Test
    fun storePartForAudioWithApplause() {
        storePart(sourceFilesFolderPath = "$testSignalResourcesPath/with-applause/")
    }

    private fun storePart(sourceFilesFolderPath: String) {
        cassandraInitializer.createTable(AUDIO_PART_TABLE, AudioPartEntity::class.java)
        val audioSignalList = File(sourceFilesFolderPath).listFiles()
            .filter { it.extension == "json" }
            .map { signalJsonFile -> MAPPER.readValue<AudioSignalKt>(signalJsonFile) }
        audioSignalList.forEach {
            val storedPart = audioSignalDao.storeAudioSignalPart(audioSignal = it)
            val actualPart = audioSignalDao.retrieveAudioSignalPart(audioFileName = storedPart.audioFileName, index = storedPart.index)
            assertNotNull(actualPart)
            val actualBytes = ByteArray(actualPart?.content?.remaining() ?: 0)
            actualPart?.content?.get(actualBytes)
            assertThat(actualBytes, Is(equalTo(it.dataInBytes)))
        }
        cassandraInitializer.dropTable(AUDIO_PART_TABLE)
    }

    @Test
    fun storeSignalPartsForAudioWithBackgroundNoiseAndLowVoiceVolume() {
        storeSignalParts(sourceFilesFolderPath = "$testSignalResourcesPath/background-noise-low-volume/")
    }

    @Test
    fun storeSignalPartsForAudioWithStrongBackgroundNoise() {
        storeSignalParts(sourceFilesFolderPath = "$testSignalResourcesPath/strong-background-noise/")
    }

    @Test
    fun storeSignalPartsForAudioWithApplause() {
        storeSignalParts(sourceFilesFolderPath = "$testSignalResourcesPath/with-applause/")
    }

    private fun storeSignalParts(sourceFilesFolderPath: String) {
        val audioSignalList = File(sourceFilesFolderPath).listFiles()
            .filter { it.extension == "json" }
            .map { signalJsonFile -> MAPPER.readValue<AudioSignalKt>(signalJsonFile) }
        audioSignalList.forEach {
            assertTrue(audioSignalDao.storeAudioSignal(audioSignal = it))
        }
        val actualAudioSignalSet = audioSignalDao.retrieveAudioSignalFromRange(
            key = audioSignalList[0].entityName + "_" + audioSignalList[0].audioFileName,
            min = 1.0,
            max = audioSignalList.size.toDouble()
        )
        assertTrue(actualAudioSignalSet?.isNotEmpty() ?: false)
        assertThat(actualAudioSignalSet?.size, Is(equalTo(audioSignalList.size)))
        audioSignalList.forEach {
            assertTrue(actualAudioSignalSet?.contains(it) ?: false)
        }
    }

}

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [SignalRmsDaoConfig::class])
class AudioSignalRmsDaoIntegrationTest {

    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao

    private val thisClass: Class<AudioSignalRmsDaoIntegrationTest> = this.javaClass
    private val testSignalRmsResourcesPath: String = thisClass.getResource("/signal").path
    private val signalRmsListType: CollectionType

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = RedisInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    init {
        signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfoKt::class.java)
    }

    @Test
    fun storeSignalRmsForAudioWithBackgroundNoiseAndLowVoiceVolume() {
        storeSignalRms(
            sourceFilePath = "$testSignalRmsResourcesPath/background-noise-low-volume/background-noise-low-volume.json",
            minIndex = 0.0,
            maxIndex = 43.5
        )
    }

    @Test
    fun storeSignalRmsForAudioWithStrongBackgroundNoise() {
        storeSignalRms(
            sourceFilePath = "$testSignalRmsResourcesPath/strong-background-noise/strong-background-noise.json",
            minIndex = 0.0,
            maxIndex = 40.9
        )
    }

    @Test
    fun storeSignalRmsForAudioWithApplause() {
        storeSignalRms(
            sourceFilePath = "$testSignalRmsResourcesPath/with-applause/with-applause.json",
            minIndex = 0.0,
            maxIndex = 40.9
        )
    }

    private fun storeSignalRms(sourceFilePath: String, minIndex: Double, maxIndex: Double) {
        val audioSignalRmsList: List<AudioSignalRmsInfoKt> = MAPPER.readValue(File(sourceFilePath), signalRmsListType)
        audioSignalRmsList.forEach {
            assertTrue(audioSignalRmsDao.storeAudioSignalRms(audioSignalRms = it))
        }
        val actualAudioSignalRmsSet = audioSignalRmsDao.retrieveAudioSignalRmsFromRange(
            key = audioSignalRmsList[0].entityName + "_" + audioSignalRmsList[0].audioFileName,
            min = minIndex,
            max = maxIndex
        )
        assertTrue(actualAudioSignalRmsSet?.isNotEmpty() ?: false)
        assertThat(actualAudioSignalRmsSet?.size, Is(equalTo(audioSignalRmsList.size)))
        audioSignalRmsList.forEach{
            assertTrue(actualAudioSignalRmsSet?.contains(it) ?: false)
        }
    }

}

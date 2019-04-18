package net.jcflorezr.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.TestSignalDaoConfig
import net.jcflorezr.config.TestSignalRmsDaoConfig
import net.jcflorezr.model.AudioPartEntity
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.model.AudioSignalRmsEntity
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.util.PropsUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import org.hamcrest.CoreMatchers.`is` as Is

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestSignalDaoConfig::class])
class AudioSignalDaoIntegrationTest {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    private val thisClass: Class<AudioSignalDaoIntegrationTest> = this.javaClass
    private val testSignalResourcesPath: String = thisClass.getResource("/signal").path

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = TestRedisInitializer()
        @JvmField
        @ClassRule
        val cassandraInitializer = TestCassandraInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
        private const val AUDIO_PART_TABLE = "AUDIO_PART"
    }

    @Test
    fun persistPartForAudioWithBackgroundNoiseAndLowVoiceVolume() {
        propsUtils.setTransactionId(sourceAudioFile = File("strong-background-noise"))
        persistPart(sourceFilesFolderPath = "$testSignalResourcesPath/strong-background-noise/")
    }

    @Test
    fun persistPartForAudioWithStrongBackgroundNoise() {
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume"))
        persistPart(sourceFilesFolderPath = "$testSignalResourcesPath/background-noise-low-volume/")
    }

    @Test
    fun persistPartForAudioWithApplause() {
        propsUtils.setTransactionId(sourceAudioFile = File("with-applause"))
        persistPart(sourceFilesFolderPath = "$testSignalResourcesPath/with-applause/")
    }

    private fun persistPart(sourceFilesFolderPath: String) {
        cassandraInitializer.createTable(AUDIO_PART_TABLE, AudioPartEntity::class.java)
        val audioSignalList = File(sourceFilesFolderPath).listFiles()
            .filter { it.extension == "json" }
            .map { signalJsonFile -> MAPPER.readValue<AudioSignal>(signalJsonFile) }
        val actualAudioSignalList = ArrayList<AudioPartEntity>()
        audioSignalList.forEach {
            val storedPart = audioSignalDao.persistAudioSignalPart(audioSignal = it)
            val actualPart = audioSignalDao.retrieveAudioSignalPart(audioFileName = storedPart.audioFileName, index = storedPart.index)
            assertNotNull(actualPart)
            val actualBytes = ByteArray(actualPart?.content?.remaining() ?: 0)
            actualPart?.content?.get(actualBytes)
            assertThat(actualBytes, Is(equalTo(it.dataInBytes)))
            actualAudioSignalList.add(actualPart!!)
        }
        assertThat(audioSignalList.size, Is(equalTo(actualAudioSignalList.size)))
        cassandraInitializer.dropTable(AUDIO_PART_TABLE)
    }

    @Test
    fun storeSignalPartsForAudioWithBackgroundNoiseAndLowVoiceVolume() {
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume"))
        storeSignalParts(sourceFilesFolderPath = "$testSignalResourcesPath/background-noise-low-volume/")
    }

    @Test
    fun storeSignalPartsForAudioWithStrongBackgroundNoise() {
        propsUtils.setTransactionId(sourceAudioFile = File("strong-background-noise"))
        storeSignalParts(sourceFilesFolderPath = "$testSignalResourcesPath/strong-background-noise/")
    }

    @Test
    fun storeSignalPartsForAudioWithApplause() {
        propsUtils.setTransactionId(sourceAudioFile = File("with-applause"))
        storeSignalParts(sourceFilesFolderPath = "$testSignalResourcesPath/with-applause/")
    }

    private fun storeSignalParts(sourceFilesFolderPath: String) {
        val audioSignalList = File(sourceFilesFolderPath).listFiles()
            .filter { it.extension == "json" }
            .map { signalJsonFile -> MAPPER.readValue<AudioSignal>(signalJsonFile) }
        audioSignalList.forEach {
            assertTrue(audioSignalDao.storeAudioSignal(audioSignal = it))
        }
        val actualAudioSignalSet = audioSignalDao.retrieveAudioSignalsFromRange(
            key = audioSignalList[0].entityName + "_" + audioSignalList[0].audioFileName,
            min = 0.0,
            max = audioSignalList.size.toDouble()
        )
        assertTrue(actualAudioSignalSet.isNotEmpty())
        assertThat(actualAudioSignalSet.size, Is(equalTo(audioSignalList.size)))
        audioSignalList.forEach {
            assertTrue(actualAudioSignalSet.contains(it))
        }
    }
}

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestSignalRmsDaoConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AudioSignalRmsDaoIntegrationTest {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao

    private val thisClass: Class<AudioSignalRmsDaoIntegrationTest> = this.javaClass
    private val testSignalRmsResourcesPath: String = thisClass.getResource("/rms").path
    private val signalRmsListType: CollectionType

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = TestRedisInitializer()
        @JvmField
        @ClassRule
        val cassandraInitializer = TestCassandraInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
        private const val AUDIO_SIGNAL_RMS_TABLE = "AUDIO_SIGNAL_RMS"
    }

    init {
        signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfo::class.java)
    }

    @Test
    fun storeSignalRmsForAudioWithBackgroundNoiseAndLowVoiceVolume() {
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume"))
        storeSignalRms(
            sourceFilePath = "$testSignalRmsResourcesPath/background-noise-low-volume/background-noise-low-volume.json",
            minIndex = 0.0,
            maxIndex = 43.5
        )
    }

    @Test
    fun storeSignalRmsForAudioWithStrongBackgroundNoise() {
        propsUtils.setTransactionId(sourceAudioFile = File("strong-background-noise"))
        storeSignalRms(
            sourceFilePath = "$testSignalRmsResourcesPath/strong-background-noise/strong-background-noise.json",
            minIndex = 0.0,
            maxIndex = 40.9
        )
    }

    @Test
    fun storeSignalRmsForAudioWithApplause() {
        propsUtils.setTransactionId(sourceAudioFile = File("with-applause"))
        storeSignalRms(
            sourceFilePath = "$testSignalRmsResourcesPath/with-applause/with-applause.json",
            minIndex = 0.0,
            maxIndex = 40.9
        )
    }

    private fun storeSignalRms(sourceFilePath: String, minIndex: Double, maxIndex: Double) {
        cassandraInitializer.createTable(AUDIO_SIGNAL_RMS_TABLE, AudioSignalRmsEntity::class.java)
        val audioSignalRmsList: List<AudioSignalRmsInfo> = MAPPER.readValue(File(sourceFilePath), signalRmsListType)
        runBlocking {
            audioSignalRmsDao.storeAudioSignalsRms(audioSignalRmsList)
        }
        val actualAudioSignalRmsSet = audioSignalRmsDao.retrieveAudioSignalsRmsFromRange(
            key = audioSignalRmsList[0].entityName + "_" + audioSignalRmsList[0].audioFileName,
            min = minIndex,
            max = maxIndex
        )
        assertTrue(actualAudioSignalRmsSet.isNotEmpty())
        assertThat(actualAudioSignalRmsSet.size, Is(equalTo(audioSignalRmsList.size)))
        audioSignalRmsList.forEach {
            assertTrue(actualAudioSignalRmsSet.contains(it))
        }
        val actualPersistedAudioSignalRmsList = audioSignalRmsDao.retrieveAllAudioSignalsRmsPersisted(
            audioFileName = audioSignalRmsList[0].audioFileName
        )
        assertTrue(actualPersistedAudioSignalRmsList.isNotEmpty())
        assertThat(actualPersistedAudioSignalRmsList.size, Is(equalTo(audioSignalRmsList.size)))
        audioSignalRmsList.forEach {
            assertTrue(actualPersistedAudioSignalRmsList.contains(AudioSignalRmsEntity(audioSignalRms = it)))
        }
        cassandraInitializer.dropTable(AUDIO_SIGNAL_RMS_TABLE)
    }
}

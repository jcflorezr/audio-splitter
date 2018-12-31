package net.jcflorezr.persistence

import biz.source_code.dsp.model.AudioSignalKt
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.config.TestRedisConfig
import net.jcflorezr.model.AudioSignalRmsInfoKt
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import redis.embedded.RedisServer
import java.io.File
import org.hamcrest.CoreMatchers.`is` as Is

class RedisDaoIntegrationTest : TestRule {

    override fun apply(statement: Statement, description: Description): Statement {
        return object: Statement() {
            override fun evaluate() {
                println("starting embedded Redis")
                val port = 6379
                val redisServer = RedisServer(port)
                redisServer.start()
                // Giving some time while the database is up
                Thread.sleep(1000L)
                try {
                    statement.evaluate()
                } finally {
                    println("stopping embedded Redis")
                    redisServer.stop()
                }
            }
        }
    }

}

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestRedisConfig::class])
class AudioSignalDaoIntegrationTest {

    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    private val thisClass: Class<AudioSignalDaoIntegrationTest> = this.javaClass
    private val testSignalResourcesPath: String = thisClass.getResource("/sound/").path

    companion object {
        @JvmField
        @ClassRule
        val initializer = RedisDaoIntegrationTest()
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    @Test
    fun storeAudioWithBackgroundNoiseAndLowVoiceVolumeSignalParts() {
        val audioSignalList = File(testSignalResourcesPath + "background-noise-low-volume/").listFiles()
            .filter { it.extension == "json" }
            .map { signalJsonFile -> MAPPER.readValue<AudioSignalKt>(signalJsonFile) }
        audioSignalList.forEach {
            assertTrue(audioSignalDao.storeAudioSignal(audioSignal = it))
        }
        val actualAudioSignalSet =
            audioSignalDao.retrieveAudioSignalFromRange(
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

    @Test
    fun storeAudioWithStrongBackgroundNoiseSignalParts() {
        val audioSignalList = File(testSignalResourcesPath + "strong-background-noise/").listFiles()
            .filter { it.extension == "json" }
            .map { signalJsonFile -> MAPPER.readValue<AudioSignalKt>(signalJsonFile) }
        audioSignalList.forEach {
            assertTrue(audioSignalDao.storeAudioSignal(audioSignal = it))
        }
        val actualAudioSignalSet =
            audioSignalDao.retrieveAudioSignalFromRange(
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

    @Test
    fun storeAudioWithApplauseSignalParts() {
        val audioSignalList = File(testSignalResourcesPath + "with-applause/").listFiles()
            .filter { it.extension == "json" }
            .map { signalJsonFile -> MAPPER.readValue<AudioSignalKt>(signalJsonFile) }
        audioSignalList.forEach {
            assertTrue(audioSignalDao.storeAudioSignal(audioSignal = it))
        }
        val actualAudioSignalSet =
            audioSignalDao.retrieveAudioSignalFromRange(
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
@ContextConfiguration(classes = [TestRedisConfig::class])
class AudioSignalRmsDaoIntegrationTest {

    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao

    private val thisClass: Class<AudioSignalRmsDaoIntegrationTest> = this.javaClass
    private val testSignalRmsResourcesPath: String = thisClass.getResource("/signal/").path
    private val signalRmsListType: CollectionType

    companion object {
        @JvmField
        @ClassRule
        val initializer = RedisDaoIntegrationTest()
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    init {
        signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfoKt::class.java)
    }

    @Test
    fun storeAudioWithBackgroundNoiseAndLowVoiceVolumeSignalRms() {
        val audioSignalRmsList: List<AudioSignalRmsInfoKt> =
                MAPPER.readValue(File(testSignalRmsResourcesPath + "background-noise-low-volume/background-noise-low-volume.json"), signalRmsListType)
        audioSignalRmsList.forEach {
            assertTrue(audioSignalRmsDao.storeAudioSignalRms(audioSignalRms = it))
        }
        val actualAudioSignalRmsSet =
                audioSignalRmsDao.retrieveAudioSignalRmsFromRange(
                    key = audioSignalRmsList[0].entityName + "_" + audioSignalRmsList[0].audioFileName,
                    min = 0.0,
                    max = 43.5
                )
        assertTrue(actualAudioSignalRmsSet?.isNotEmpty() ?: false)
        assertThat(actualAudioSignalRmsSet?.size, Is(equalTo(audioSignalRmsList.size)))
        audioSignalRmsList.forEach{
            assertTrue(actualAudioSignalRmsSet?.contains(it) ?: false)
        }
    }

    @Test
    fun storeAudioWithStrongBackgroundNoiseSignalRms() {
        val audioSignalRmsList: List<AudioSignalRmsInfoKt> =
                MAPPER.readValue(File(testSignalRmsResourcesPath + "strong-background-noise/strong-background-noise.json"), signalRmsListType)
        audioSignalRmsList.forEach {
            assertTrue(audioSignalRmsDao.storeAudioSignalRms(audioSignalRms = it))
        }
        val actualAudioSignalRmsSet =
                audioSignalRmsDao.retrieveAudioSignalRmsFromRange(
                    key = audioSignalRmsList[0].entityName + "_" + audioSignalRmsList[0].audioFileName,
                    min = 0.0,
                    max = 40.9
                )
        assertTrue(actualAudioSignalRmsSet?.isNotEmpty() ?: false)
        assertThat(actualAudioSignalRmsSet?.size, Is(equalTo(audioSignalRmsList.size)))
        audioSignalRmsList.forEach{
            assertTrue(actualAudioSignalRmsSet?.contains(it) ?: false)
        }
    }

    @Test
    fun storeAudioWithApplauseSignalRms() {
        val audioSignalRmsList: List<AudioSignalRmsInfoKt> =
                MAPPER.readValue(File(testSignalRmsResourcesPath + "with-applause/with-applause.json"), signalRmsListType)
        audioSignalRmsList.forEach {
            assertTrue(audioSignalRmsDao.storeAudioSignalRms(audioSignalRms = it))
        }
        val actualAudioSignalRmsSet =
                audioSignalRmsDao.retrieveAudioSignalRmsFromRange(
                    key = audioSignalRmsList[0].entityName + "_" + audioSignalRmsList[0].audioFileName,
                    min = 0.0,
                    max = 40.9
                )
        assertTrue(actualAudioSignalRmsSet?.isNotEmpty() ?: false)
        assertThat(actualAudioSignalRmsSet?.size, Is(equalTo(audioSignalRmsList.size)))
        audioSignalRmsList.forEach{
            assertTrue(actualAudioSignalRmsSet?.contains(it) ?: false)
        }
    }

}

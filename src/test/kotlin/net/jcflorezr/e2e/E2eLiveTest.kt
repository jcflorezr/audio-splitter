package net.jcflorezr.e2e

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.jcflorezr.config.RootConfig
import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.SourceFileDao
import net.jcflorezr.dao.TestCassandraInitializer
import net.jcflorezr.dao.TestRedisInitializer
import net.jcflorezr.entrypoint.AudioSplitter
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioClipInfoEntity
import net.jcflorezr.model.AudioFileMetadataEntity
import net.jcflorezr.model.AudioPartEntity
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.model.AudioSignalRmsEntity
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.model.GroupedAudioClipInfoEntity
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.util.AudioFormats
import net.jcflorezr.util.PropsUtils
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers.`is` as Is
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
import org.testcontainers.shaded.org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileInputStream

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [RootConfig::class])
class E2eLiveTest {

    @Autowired
    private lateinit var sourceFileDao: SourceFileDao
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao
    @Autowired
    private lateinit var audioSignalRmsDao: AudioSignalRmsDao
    @Autowired
    private lateinit var audioClipDao: AudioClipDao

    private val logger = KotlinLogging.logger { }

    private val thisClass: Class<E2eLiveTest> = this.javaClass
    private val testResourcesPath: String
    private val signalResourcesPath: String
    private val rmsResourcesPath: String
    private val clipResourcesPath: String
    private val tempConvertedFilesPath: String

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = TestRedisInitializer()
        @JvmField
        @ClassRule
        val cassandraInitializer = TestCassandraInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
        private const val AUDIO_FILE_METADATA_TABLE = "AUDIO_FILE_METADATA"
        private const val AUDIO_PART_TABLE = "AUDIO_PART"
        private const val AUDIO_SIGNAL_RMS_TABLE = "AUDIO_SIGNAL_RMS"
        private const val AUDIO_CLIP_INFO_TABLE = "AUDIO_CLIP_INFO"
        private const val GROUPED_AUDIO_CLIP_INFO_TABLE = "GROUPED_AUDIO_CLIP_INFO"
    }

    init {
        val cassandraContainer = TestCassandraInitializer.cassandraDockerContainer
        val redisContainer = TestRedisInitializer.redisDockerContainer
        System.setProperty("cassandra.contactpoints", cassandraContainer.containerIpAddress)
        System.setProperty("cassandra.port", cassandraContainer.getMappedPort(TestCassandraInitializer.cassandraPort).toString())
        System.setProperty("cassandra.keyspace", "audio_splitter")
        System.setProperty("redis.hostname", redisContainer.containerIpAddress)
        System.setProperty("redis.port", redisContainer.getMappedPort(TestRedisInitializer.redisPort).toString())
        testResourcesPath = thisClass.getResource("/e2e").path
        tempConvertedFilesPath = thisClass.getResource("/temp-converted-files").path
        signalResourcesPath = thisClass.getResource("/signal").path
        rmsResourcesPath = thisClass.getResource("/rms").path
        clipResourcesPath = thisClass.getResource("/clip").path
        cassandraInitializer.createTable(AUDIO_FILE_METADATA_TABLE, AudioFileMetadataEntity::class.java)
        cassandraInitializer.createTable(AUDIO_PART_TABLE, AudioPartEntity::class.java)
        cassandraInitializer.createTable(AUDIO_SIGNAL_RMS_TABLE, AudioSignalRmsEntity::class.java)
        cassandraInitializer.createTable(AUDIO_CLIP_INFO_TABLE, AudioClipInfoEntity::class.java)
        cassandraInitializer.createTable(GROUPED_AUDIO_CLIP_INFO_TABLE, GroupedAudioClipInfoEntity::class.java)
    }

    @Autowired
    private lateinit var audioSplitter: AudioSplitter

    @Test
    fun splitAudioIntoClips() = runBlocking {
        val audioFilesNames = listOf("background-noise-low-volume", "with-applause", "strong-background-noise")
        coroutineScope {
            audioFilesNames.forEach { audioFileName ->
                launch {
                    audioSplitter.splitAudioIntoClips(
                        configuration = InitialConfiguration(
                            audioFileLocation = "$signalResourcesPath/$audioFileName/$audioFileName${AudioFormats.WAV.extension}",
                            outputDirectory = tempConvertedFilesPath
                        )
                    )
                }
            }
        }

        delay(4000L) // giving some time to system to finish before starting to validate data persisted in cassandra

        audioFilesNames.forEach { audioFileName ->
            try {
                val transactionId = PropsUtils.getTransactionId(audioFileName)

                testAudioSource(audioFileName, transactionId)
                testAudioContent(audioFileName, transactionId)
                testAudioRms(audioFileName, transactionId)
                testAudioClipsInfo(audioFileName, transactionId)
                testGroupedAudioClipsInfo(audioFileName, transactionId)
                testAudioClipsFiles(audioFileName, transactionId)
                testRedisWasFullyEmptied(audioFileName, transactionId)

            } finally {
                val transactionId = PropsUtils.getTransactionId(sourceAudioFileName = audioFileName)
                File("$tempConvertedFilesPath/$transactionId").deleteRecursively()
            }
        }

    }

    private fun testAudioSource(audioFileName: String, transactionId: String) {
        val audioMetadataLocation = "$testResourcesPath/audio-metadata/$audioFileName.json"
        val expectedAudioMetadata = MAPPER.readValue<AudioFileMetadataEntity>(File(audioMetadataLocation))
        val actualAudioMetadata = sourceFileDao.retrieveAudioFileMetadata("$audioFileName${AudioFormats.WAV.extension}")

        logger.info { "[$transactionId][TEST] testing audio metadata" }

        assertNotNull("No metadata found for audio file: $audioFileName", actualAudioMetadata)
        assertThat(actualAudioMetadata, Is(equalTo(expectedAudioMetadata)))
    }

    private fun testAudioContent(audioFileName: String, transactionId: String) {
        val audioPartsLocation = "$signalResourcesPath/$audioFileName"
        val expectedAudioParts = File(audioPartsLocation).listFiles().filter { it.extension == "json" }
        val actualAudioParts = expectedAudioParts.map {
            val expectedAudioPart = MAPPER.readValue(it, AudioSignal::class.java)
            assertThat(FilenameUtils.getBaseName(expectedAudioPart.audioFileName), Is(equalTo(audioFileName)))
            val actualAudioPart = audioSignalDao.retrieveAudioSignalPart(
                audioFileName = FilenameUtils.getName("$audioFileName${AudioFormats.WAV.extension}"),
                index = expectedAudioPart.index
            )

            logger.info { "[$transactionId][TEST] testing audio part ==> index: ${expectedAudioPart.index}" }

            assertNotNull("No audio part found. Audio file name: ${expectedAudioPart.audioFileName} - index: ${expectedAudioPart.index}",
                actualAudioPart)
            assertThat(actualAudioPart!!.sampleRate, Is(equalTo(expectedAudioPart.sampleRate)))
            assertThat(actualAudioPart.channels, Is(equalTo(expectedAudioPart.audioSourceInfo.channels)))
            assertThat(actualAudioPart.sampleSize, Is(equalTo(expectedAudioPart.audioSourceInfo.sampleSize)))
            assertThat(actualAudioPart.sampleSizeInBits, Is(equalTo(expectedAudioPart.audioSourceInfo.sampleSizeInBits)))
            val actualBytes = ByteArray(actualAudioPart.content.remaining())
            actualAudioPart.content.get(actualBytes)
            assertThat(actualBytes, Is(equalTo(expectedAudioPart.dataInBytes)))
            actualAudioPart
        }
        assertThat(expectedAudioParts.size, Is(equalTo(actualAudioParts.size)))
    }

    private fun testAudioRms(audioFileName: String, transactionId: String) {
        val signalRmsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSignalRmsInfo::class.java)
        val expectedSignalRmsList: List<AudioSignalRmsInfo> =
            MAPPER.readValue(File("$rmsResourcesPath/$audioFileName/$audioFileName.json"), signalRmsListType)
        val actualSignalRmsList = audioSignalRmsDao.retrieveAllAudioSignalsRmsPersisted("$audioFileName${AudioFormats.WAV.extension}")

        assertThat(actualSignalRmsList.size, Is(equalTo(expectedSignalRmsList.size)))

        expectedSignalRmsList.map { AudioSignalRmsEntity(it) }
        .forEach { expectedSignalRms ->
            logger.info { "[$transactionId][TEST] testing audio rms ==> index: ${expectedSignalRms.index}" }
            assertTrue("signalRms ----> $expectedSignalRms was not found", actualSignalRmsList.contains(expectedSignalRms))
        }

    }

    private fun testAudioClipsInfo(audioFileName: String, transactionId: String) {
        val clipInfoListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
        val expectedClipInfoList: List<AudioClipInfo> =
            MAPPER.readValue(File("$clipResourcesPath/$audioFileName/$audioFileName.json"), clipInfoListType)
        val actualClipInfoList = audioClipDao.retrieveAllAudioClipsInfoPersisted("$audioFileName${AudioFormats.WAV.extension}")

        assertThat(actualClipInfoList.size, Is(equalTo(expectedClipInfoList.size)))

        expectedClipInfoList.map { AudioClipInfoEntity(it) }
        .forEach { expectedClipInfo ->
            logger.info { "[$transactionId][TEST] testing audio clip info ==> index: ${expectedClipInfo.initialPositionInSeconds}" }
            assertTrue("audioClipInfo ----> $expectedClipInfo was not found", actualClipInfoList.contains(expectedClipInfo))
        }
    }

    private fun testGroupedAudioClipsInfo(audioFileName: String, transactionId: String) {
        val groupedClipInfoListType = MAPPER.typeFactory.constructCollectionType(List::class.java, GroupedAudioClipInfoEntity::class.java)
        val expectedGroupedClipInfoList: List<GroupedAudioClipInfoEntity> =
            MAPPER.readValue(File("$clipResourcesPath/$audioFileName/grouped-clips/$audioFileName.json"), groupedClipInfoListType)
        val actualGroupedClipInfoList = audioClipDao.retrieveAllGroupedAudioClipsInfoPersisted("$audioFileName${AudioFormats.WAV.extension}")

        expectedGroupedClipInfoList.forEach { groupedClipInfo ->
            logger.info { "[$transactionId][TEST] testing grouped audio clip info ==> " +
                "hours: ${groupedClipInfo.firstClipHours} - minutes: ${groupedClipInfo.firstClipMinutes}" +
                "seconds: ${groupedClipInfo.firstClipSeconds} - tenths: ${groupedClipInfo.firstClipTenthsOfSecond}" }
            assertTrue("audioClipInfo ----> $groupedClipInfo was not found", actualGroupedClipInfoList.contains(groupedClipInfo))
        }
    }

    private fun testAudioClipsFiles(audioFileName: String, transactionId: String) {
        val expectedAudioClipsSignalFolder = "$clipResourcesPath/$audioFileName/signal"
        val expectedAudioClipsFilesFolder = "$clipResourcesPath/$audioFileName/audio-file"
        val actualAudioClipsFilesFolder = "$tempConvertedFilesPath/$transactionId"
        val expectedClipsFiles = File(expectedAudioClipsSignalFolder).listFiles()
        expectedClipsFiles.map { expectedClipSignalFile ->
            val baseClipFileName = expectedClipSignalFile.nameWithoutExtension
            val actualAudioClipFile = File("$actualAudioClipsFilesFolder/$baseClipFileName${AudioFormats.FLAC.extension}")
            val expectedAudioClipFile = File("$expectedAudioClipsFilesFolder/$baseClipFileName${AudioFormats.FLAC.extension}")

            logger.info { "[$transactionId][TEST] testing generated audio clip file ==> ${actualAudioClipFile.absolutePath}" }

            assertThat(actualAudioClipFile.name, Is(equalTo(expectedAudioClipFile.name)))
            assertTrue(IOUtils.contentEquals(FileInputStream(actualAudioClipFile), FileInputStream(expectedAudioClipFile)))
            true
        }.let {
            assertTrue("None audio clips files where tested", it.isNotEmpty())
            assertThat("There were audio clips files that were not tested", it.size, Is(equalTo(expectedClipsFiles.size)))
        }
    }

    private fun testRedisWasFullyEmptied(audioFileName: String, transactionId: String) {
        logger.info { "[$transactionId][TEST] testing if there were remaining rows in Redis for file ==> $audioFileName" }

        val signalsList = audioSignalDao.retrieveAllAudioSignals(key = "audioSignal_$audioFileName")
        assertTrue("There were ${signalsList.size} audio signals that were not removed from database ----> $signalsList",
            signalsList.isEmpty())
        val signalsRmsList = audioSignalRmsDao.retrieveAllAudioSignalsRms(key = "audioSignalRms_$audioFileName")
        assertTrue("There were ${signalsRmsList.size} audio signals rms that were not removed from database ----> $signalsRmsList",
            signalsRmsList.isEmpty())
        val audioClipInfoList = audioClipDao.retrieveAllAudioClipsInfo(key = "audioClipInfo_$audioFileName")
        assertTrue("There were ${audioClipInfoList.size} audio clips info that were not removed from database ----> $audioClipInfoList",
            audioClipInfoList.isEmpty())
    }

}
package net.jcflorezr.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.TestClipDaoConfig
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioClipInfoEntity
import net.jcflorezr.model.GroupedAudioClipInfoEntity
import net.jcflorezr.util.PropsUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
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
@ContextConfiguration(classes = [TestClipDaoConfig::class])
class ClipDaoIntegrationTest {

    @Autowired
    private lateinit var audioClipDao: AudioClipDao

    private val thisClass: Class<ClipDaoIntegrationTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/clip").path

    companion object {
        @JvmField
        @ClassRule
        val redisInitializer = TestRedisInitializer()
        @JvmField
        @ClassRule
        val cassandraInitializer = TestCassandraInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
        private const val AUDIO_CLIP_INFO_TABLE = "AUDIO_CLIP_INFO"
        private const val GROUPED_AUDIO_CLIP_INFO_TABLE = "GROUPED_AUDIO_CLIP_INFO"
    }

    @Test
    fun persistClipInfoForAudioWithBackgroundNoiseAndLowVoiceVolume() {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("strong-background-noise"))
        persistClipInfo(clipInfoFileName = "strong-background-noise")
    }

    @Test
    fun persistClipInfoForAudioWithStrongBackgroundNoise() {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("background-noise-low-volume"))
        persistClipInfo(clipInfoFileName = "background-noise-low-volume")
    }

    @Test
    fun persistClipInfoForAudioWithApplause() {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("with-applause"))
        persistClipInfo(clipInfoFileName = "with-applause")
    }

    private fun persistClipInfo(clipInfoFileName: String) = runBlocking {
        cassandraInitializer.createTable(AUDIO_CLIP_INFO_TABLE, AudioClipInfoEntity::class.java)
        val clipInfoListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
        val expectedAudioClipsInfoList: List<AudioClipInfo> = MAPPER.readValue(
            File("$testResourcesPath/$clipInfoFileName/$clipInfoFileName.json"), clipInfoListType)
        expectedAudioClipsInfoList.forEach {
            audioClipDao.persistAudioClipInfo(audioClipInfo = it)
        }
        val actualAudioClipsInfoList = audioClipDao.retrieveAllAudioClipsInfoPersisted(expectedAudioClipsInfoList.first().audioFileName)
        assertThat(actualAudioClipsInfoList.size, Is(equalTo(expectedAudioClipsInfoList.size)))
        expectedAudioClipsInfoList.map { AudioClipInfoEntity(it) }
        .forEach { expectedClipInfo ->
            assertTrue("audioClipInfo ----> $expectedClipInfo was not found", actualAudioClipsInfoList.contains(expectedClipInfo))
        }
        cassandraInitializer.dropTable(AUDIO_CLIP_INFO_TABLE)
    }

    @Test
    fun persistGroupedClipInfoForAudioWithBackgroundNoiseAndLowVoiceVolume() {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("strong-background-noise"))
        persistGroupedClipInfo(clipInfoFileName = "strong-background-noise")
    }

    @Test
    fun persistGroupedClipInfoForAudioWithStrongBackgroundNoise() {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("background-noise-low-volume"))
        persistGroupedClipInfo(clipInfoFileName = "background-noise-low-volume")
    }

    @Test
    fun persistGroupedClipInfoForAudioWithApplause() {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("with-applause"))
        persistGroupedClipInfo(clipInfoFileName = "with-applause")
    }

    private fun persistGroupedClipInfo(clipInfoFileName: String) = runBlocking {
        cassandraInitializer.createTable(GROUPED_AUDIO_CLIP_INFO_TABLE, GroupedAudioClipInfoEntity::class.java)
        val clipInfoListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
        val expectedGroupedClipsInfoList: List<AudioClipInfo> = MAPPER.readValue(
            File("$testResourcesPath/$clipInfoFileName/$clipInfoFileName.json"), clipInfoListType)
        expectedGroupedClipsInfoList.forEach {
            audioClipDao.persistGroupedAudioClipInfo(firstAudioClipInfo = it, lastAudioClipInfo = it)
        }
        val actualGroupedClipsInfoList = audioClipDao.retrieveAllGroupedAudioClipsInfoPersisted(expectedGroupedClipsInfoList.first().audioFileName)
        assertThat(actualGroupedClipsInfoList.size, Is(equalTo(expectedGroupedClipsInfoList.size)))
        expectedGroupedClipsInfoList.map { GroupedAudioClipInfoEntity(firstAudioClipInfo = it, lastAudioClipInfo = it) }
        .forEach { expectedGroupedClipInfo ->
            assertTrue("groupedAudioClipInfo ----> $expectedGroupedClipInfo was not found", actualGroupedClipsInfoList.contains(expectedGroupedClipInfo))
        }
        cassandraInitializer.dropTable(GROUPED_AUDIO_CLIP_INFO_TABLE)
    }

    @Test
    fun storeClipsInfoForAudioWithBackgroundNoiseAndLowVoiceVolume() {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("background-noise-low-volume"))
        storeClipsInfo(clipInfoFileName = "background-noise-low-volume")
    }

    @Test
    fun storeClipsInfoForAudioWithStrongBackgroundNoise() {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("strong-background-noise"))
        storeClipsInfo(clipInfoFileName = "strong-background-noise")
    }

    @Test
    fun storeClipsInfoForAudioWithApplause() {
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File("with-applause"))
        storeClipsInfo(clipInfoFileName = "with-applause")
    }

    private fun storeClipsInfo(clipInfoFileName: String) = runBlocking {
        cassandraInitializer.createTable(AUDIO_CLIP_INFO_TABLE, AudioClipInfoEntity::class.java)
        val clipInfoListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
        val expectedClipsInfoList: List<AudioClipInfo> = MAPPER.readValue(
            File("$testResourcesPath/$clipInfoFileName/$clipInfoFileName.json"), clipInfoListType)
        expectedClipsInfoList.forEach { audioClipDao.storeAudioClipInfo(audioClipInfo = it) }
        val actualClipsInfoList = audioClipDao.retrieveAllAudioClipsInfo(
            key = expectedClipsInfoList[0].entityName + "_" + expectedClipsInfoList[0].audioFileName
        )
        assertTrue(actualClipsInfoList.isNotEmpty())
        assertThat(actualClipsInfoList.size, Is(equalTo(actualClipsInfoList.size)))
        actualClipsInfoList.forEach {
            assertTrue(actualClipsInfoList.contains(it))
        }
        cassandraInitializer.dropTable(AUDIO_CLIP_INFO_TABLE)
    }
}

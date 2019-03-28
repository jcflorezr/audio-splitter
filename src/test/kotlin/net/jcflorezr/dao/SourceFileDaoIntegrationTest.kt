package net.jcflorezr.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.TestSourceFileDaoConfig
import net.jcflorezr.model.AudioFileMetadataEntity
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.util.PropsUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
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
@ContextConfiguration(classes = [TestSourceFileDaoConfig::class])
class SourceFileDaoIntegrationTest {

    @Autowired
    private lateinit var sourceFileDao: SourceFileDao

    private val thisClass: Class<SourceFileDaoIntegrationTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/entrypoint/").path

    companion object {
        @JvmField
        @ClassRule
        val initializer = TestCassandraInitializer()
        private val MAPPER = ObjectMapper().registerKotlinModule()
        private const val AUDIO_FILE_METADATA_TABLE = "AUDIO_FILE_METADATA"
    }

    // TODO: this test is failing because it cannot connect to cassandra
    @Test
    fun storeInitialConfigurationWithAudioMetadata() = runBlocking {
        initializer.createTable(AUDIO_FILE_METADATA_TABLE, AudioFileMetadataEntity::class.java)
        val initialConfiguration = MAPPER.readValue<InitialConfiguration>(File(testResourcesPath + "initial-configuration.json"))
        PropsUtils.setTransactionIdProperty(sourceAudioFile = File(initialConfiguration.audioFileLocation))
        val storedAudioMetadata = sourceFileDao.persistAudioFileMetadata(initialConfiguration)
        val actualAudioMetadata = sourceFileDao.retrieveAudioFileMetadata(storedAudioMetadata.audioFileName)
        assertThat(actualAudioMetadata, Is(equalTo(storedAudioMetadata)))
        initializer.dropTable(AUDIO_FILE_METADATA_TABLE)
    }

}
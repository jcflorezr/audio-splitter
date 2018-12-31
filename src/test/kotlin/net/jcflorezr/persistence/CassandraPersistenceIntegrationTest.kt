package net.jcflorezr.persistence

import com.datastax.driver.core.Cluster
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.config.TestCassandraConfig
import net.jcflorezr.model.AudioFileMetadataEntity
import net.jcflorezr.model.InitialConfiguration
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.cassandra.core.CassandraAdminTemplate
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter
import org.springframework.data.cassandra.core.cql.CqlIdentifier
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import java.util.*
import org.hamcrest.CoreMatchers.`is` as Is

class CassandraDaoIntegrationTest : TestRule {

    companion object {
        private const val CONTACT_POINTS= "localhost"
        private const val PORT = 9142
        private const val KEYSPACE_CREATION_SCRIPT = "CREATE KEYSPACE IF NOT EXISTS AUDIO_SPLITTER WITH replication = { 'class': 'SimpleStrategy', 'replication_factor': '1' };"
        private const val KEYSPACE_ACTIVATION_SCRIPT = "USE AUDIO_SPLITTER;"
        private var cassandraAdminTemplate: CassandraAdminTemplate? = null
    }

    override fun apply(statement: Statement, description: Description): Statement {
        return object: Statement() {
            override fun evaluate() {
                // TODO: implement Logger
                println("Starting embedded Cassandra")
                EmbeddedCassandraServerHelper.startEmbeddedCassandra()
                val cluster = Cluster
                    .builder()
                    .addContactPoints(CONTACT_POINTS)
                    .withPort(PORT)
                    .build()
                val session = cluster.connect()
                session.execute(KEYSPACE_CREATION_SCRIPT)
                session.execute(KEYSPACE_ACTIVATION_SCRIPT)
                cassandraAdminTemplate = CassandraAdminTemplate(session, MappingCassandraConverter())
                // Giving some time while the keyspace is created
                Thread.sleep(1000)
                try {
                    statement.evaluate()
                } finally {
                    // TODO: implement Logger
                    println("Stopping embedded Cassandra")
                    /*
                    TODO: there is an exception thrown by cassandra driver when trying to
                        stop the database instance: java.io.IOException: Connection reset by peer
                     */
                    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
                }
            }
        }
    }

    fun createTable(tableName: String, aClass: Class<*>) {
        cassandraAdminTemplate!!.createTable(true, CqlIdentifier.cqlId(tableName), aClass, HashMap())
    }

    fun dropTable(tableName: String) {
        cassandraAdminTemplate!!.dropTable(CqlIdentifier.cqlId(tableName))
    }

}

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestCassandraConfig::class])
class SourceFileDaoImplIntegrationTest {

    @Autowired
    private lateinit var sourceFileDao: SourceFileDao

    private val thisClass: Class<SourceFileDaoImplIntegrationTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/source/").path

    companion object {
        @JvmField
        @ClassRule
        val initializer = CassandraDaoIntegrationTest()
        private val MAPPER = ObjectMapper().registerKotlinModule()
        private const val AUDIO_FILE_METADATA_TABLE = "AUDIO_FILE_METADATA"
    }

    @Test
    fun storeInitialConfigurationWithAudioMetadata() {
        initializer.createTable(AUDIO_FILE_METADATA_TABLE, AudioFileMetadataEntity::class.java)
        val initialConfiguration = MAPPER.readValue<InitialConfiguration>(File(testResourcesPath + "initial-configuration.json"))
        val storedAudioMetadata = sourceFileDao.storeAudioFileMetadata(initialConfiguration)
        val actualAudioMetadata = sourceFileDao.retrieveAudioFileMetadata(storedAudioMetadata.audioFileName)
        MatcherAssert.assertThat(actualAudioMetadata, Is(CoreMatchers.equalTo(storedAudioMetadata)))
        initializer.dropTable(AUDIO_FILE_METADATA_TABLE)
    }

}
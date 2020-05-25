package net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.sourcefileinfo.SourceFileInfoCassandraDaoIntTestDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.core.exception.NotFoundException
import net.jcflorezr.transcriber.core.exception.PersistenceException
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
internal class SourceFileInfoCassandraDaoIntTest {

    private val testResourcesPath: String = this.javaClass.getResource("/source-file-info").path
    private val mapper = ObjectMapper().registerKotlinModule()
    private val sourceFileInfoCassandraDaoIntTestDI = SourceFileInfoCassandraDaoIntTestDI
    private lateinit var sourceFileInfoCassandraDao: SourceFileInfoCassandraDao

    @BeforeAll
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(sourceFileInfoCassandraDaoIntTestDI, testContext.completing())
        sourceFileInfoCassandraDao = sourceFileInfoCassandraDaoIntTestDI.sourceFileInfoCassandraDao
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
        sourceFileInfoCassandraDaoIntTestDI.cassandraClient.close()
        testContext.completeNow()
    }

    @Test
    fun `save source file info to db and then retrieve it from db`(testContext: VertxTestContext) = runBlocking(Dispatchers.IO) {
        val expectedSourceFileInfo =
            mapper.readValue(File("$testResourcesPath/audio-file-info.json"), AudioSourceFileInfo::class.java)
        sourceFileInfoCassandraDao.save(
            sourceFileInfoCassandraRecord = SourceFileInfoCassandraRecord.fromEntity(expectedSourceFileInfo))
        val actualSourceFileInfo =
            sourceFileInfoCassandraDao.findBy(expectedSourceFileInfo.originalAudioFile).translate()
        assertThat(actualSourceFileInfo, Is(equalTo(expectedSourceFileInfo)))
        testContext.completeNow()
    }

    @Test
    fun `should throw record not found exception`(testContext: VertxTestContext) {
        val fakeAudioFileName = "any-audio-file-name"
        val actualException = Assertions.assertThrows(NotFoundException::class.java) {
            runBlocking { sourceFileInfoCassandraDao.findBy(fakeAudioFileName) }
        }
        val expectedException = PersistenceException.recordNotFoundInRepository(
            entityName = SourceFileMetadataCassandraRecord::class.java.simpleName,
            keys = mapOf(SourceFileMetadataCassandraRecord.AUDIO_FILE_NAME_COLUMN to fakeAudioFileName))
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
        testContext.completeNow()
    }
}

package net.jcflorezr.transcriber.audio.splitter.adapters.dao.sourcefileinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.sourcefileinfo.SourceFileInfoCassandraDaoTestDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.core.exception.PersistenceException
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import javax.annotation.PostConstruct

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [SourceFileInfoCassandraDaoTestDI::class])
internal class SourceFileInfoCassandraDaoTest {

    @Autowired
    private lateinit var sourceFileInfoCassandraDao: SourceFileInfoCassandraDao
    @Autowired
    private lateinit var sourceFileInfoCassandraDaoTestDI: SourceFileInfoCassandraDaoTestDI

    private val thisClass: Class<SourceFileInfoCassandraDaoTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/source-file-info").path

    @PostConstruct
    fun setUp() {
        sourceFileInfoCassandraDaoTestDI.createTable(
            "SOURCE_FILE_METADATA", SourceFileMetadataCassandraRecord::class.java)
        sourceFileInfoCassandraDaoTestDI.createTable(
            "SOURCE_FILE_CONTENT_INFO", SourceFileContentInfoCassandraRecord::class.java)
    }

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    @Test
    fun saveAndRetrieveSourceFileInfo() {
        val expectedSourceFileInfo =
            MAPPER.readValue(File("$testResourcesPath/audio-file-info.json"), AudioSourceFileInfo::class.java)
        sourceFileInfoCassandraDao.save(
            sourceFileInfoCassandraRecord = SourceFileInfoCassandraRecord.fromEntity(expectedSourceFileInfo))
        val actualSourceFileInfo =
            sourceFileInfoCassandraDao.findBy(expectedSourceFileInfo.originalAudioFile).translate()
        assertThat(actualSourceFileInfo, Is(equalTo(expectedSourceFileInfo)))
    }

    @Test
    fun shouldThrowRecordNotFoundException() {
        val fakeAudioFileName = "any-audio-file-name"
        val actualException = Assertions.assertThrows(PersistenceException::class.java) {
            sourceFileInfoCassandraDao.findBy(fakeAudioFileName)
        }
        val expectedException = PersistenceException.recordNotFoundInRepository(
            entityName = SourceFileMetadataCassandraRecord::class.java.simpleName,
            keys = mapOf(SourceFileMetadataCassandraRecord.PRIMARY_COLUMN_NAME to fakeAudioFileName))
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
    }
}
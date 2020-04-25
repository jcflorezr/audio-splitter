package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments.AudioSegmentsCassandraDaoTestDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.core.exception.FileException
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
import java.io.FileNotFoundException
import javax.annotation.PostConstruct

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AudioSegmentsCassandraDaoTestDI::class])
internal class AudioSegmentsCassandraDaoTest {

    @Autowired
    private lateinit var audioSegmentsCassandraDao: AudioSegmentsCassandraDao
    @Autowired
    private lateinit var cassandraDaoTestDI: AudioSegmentsCassandraDaoTestDI

    private val thisClass: Class<AudioSegmentsCassandraDaoTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/audio-segments").path

    @PostConstruct
    fun setUp() {
        cassandraDaoTestDI.createTable(
            AudioSegmentCassandraRecord.TABLE_NAME, AudioSegmentCassandraRecord::class.java)
    }

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    @Test
    fun saveAndRetrieveAudioSegments() {
        val filesKeyword = "single"
        val testDirectory = File(testResourcesPath).takeIf { it.exists() } ?: throw FileException.fileNotFound(testResourcesPath)
        val expectedAudioSegmentsFiles =
            testDirectory
                .listFiles { file -> !file.nameWithoutExtension.contains(filesKeyword) && !file.isDirectory }
                ?.takeIf { it.isNotEmpty() }
            ?: throw FileNotFoundException("No files with keyword '$filesKeyword' were found in directory '$testResourcesPath'")

        expectedAudioSegmentsFiles.forEach { expectedAudioSegmentsFile ->
            val audioSegmentsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSegment::class.java)
            val expectedAudioSegments: List<AudioSegment> = MAPPER.readValue(expectedAudioSegmentsFile, audioSegmentsListType)
            expectedAudioSegments.forEach { expectedAudioSegment ->
                audioSegmentsCassandraDao.save(AudioSegmentCassandraRecord.fromEntity(expectedAudioSegment))
                audioSegmentsCassandraDao.findBy(
                    expectedAudioSegment.sourceAudioFileName, expectedAudioSegment.segmentStartInSeconds)
            }
            val audioFileName = expectedAudioSegments.first().sourceAudioFileName
            val segmentStartInSeconds = expectedAudioSegments.first().segmentStartInSeconds
            val segmentEndInSeconds = expectedAudioSegments.last().segmentStartInSeconds
            val actualAudioSegments =
                audioSegmentsCassandraDao.findRange(audioFileName, segmentStartInSeconds, segmentEndInSeconds)
                    .map { it.translate() }
                    .toList()
            assertThat(actualAudioSegments.size, Is(equalTo(expectedAudioSegments.size)))
            assertThat(actualAudioSegments, Is(equalTo(expectedAudioSegments)))
        }
    }

    @Test
    fun shouldThrowRecordNotFoundException() {
        val fakeAudioFileName = "any-audio-file-name"
        val fakeSegmentStartInSecondsValue = 0.0f
        val actualException = Assertions.assertThrows(PersistenceException::class.java) {
            audioSegmentsCassandraDao.findBy(fakeAudioFileName, fakeSegmentStartInSecondsValue)
        }
        val expectedException = PersistenceException.recordNotFoundInRepository(
            entityName = AudioSegmentCassandraRecord::class.java.simpleName,
            keys = mapOf(
                AudioSegmentCassandraRecord.AUDIO_FILE_NAME_COLUMN to fakeAudioFileName,
                AudioSegmentCassandraRecord.SEGMENT_START_IN_SECONDS_COLUMN to fakeSegmentStartInSecondsValue))
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
    }
}
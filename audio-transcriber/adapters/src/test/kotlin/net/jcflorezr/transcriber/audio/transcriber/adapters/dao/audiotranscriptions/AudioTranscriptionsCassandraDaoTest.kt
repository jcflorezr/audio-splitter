package net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.audiotranscriptions.AudioTranscriptionsCassandraDaoTestDI
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
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
@ContextConfiguration(classes = [AudioTranscriptionsCassandraDaoTestDI::class])
internal class AudioTranscriptionsCassandraDaoTest {

    @Autowired
    private lateinit var audioTranscriptionsCassandraDao: AudioTranscriptionsCassandraDao
    @Autowired
    private lateinit var audioTranscriptionsCassandraDaoTestDI: AudioTranscriptionsCassandraDaoTestDI

    private val thisClass: Class<AudioTranscriptionsCassandraDaoTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/audio-clips-transcriptions").path

    @PostConstruct
    fun setUp() {
        audioTranscriptionsCassandraDaoTestDI.createTable(
            TranscriptionCassandraRecord.TABLE_NAME, TranscriptionCassandraRecord::class.java)
        audioTranscriptionsCassandraDaoTestDI.createTable(
            AlternativeCassandraRecord.TABLE_NAME, AlternativeCassandraRecord::class.java)
        audioTranscriptionsCassandraDaoTestDI.createTable(
            WordCassandraRecord.TABLE_NAME, WordCassandraRecord::class.java)
    }

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    @Test
    fun saveAndRetrieveAudioTranscriptions() {
        val filesKeyword = "aggregate"
        val testDirectory = File(testResourcesPath).takeIf { it.exists() }
                ?: throw FileException.fileNotFound(testResourcesPath)
        val expectedAudioTranscriptionsFiles =
            testDirectory.listFiles { file -> file.nameWithoutExtension.contains(filesKeyword) }?.takeIf { it.isNotEmpty() }
            ?: throw FileNotFoundException("No files with keyword '$filesKeyword' were found in directory '$testResourcesPath'")

        expectedAudioTranscriptionsFiles
            .map { expectedAudioTranscriptionsFile ->
                val expectedAudioTranscription =
                    MAPPER.readValue(expectedAudioTranscriptionsFile, AudioTranscription::class.java)
                audioTranscriptionsCassandraDao.save(AudioTranscriptionCassandraRecord.fromEntity(expectedAudioTranscription))
                expectedAudioTranscription.run {
                    audioTranscriptionsCassandraDao.findBy(sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond)
                }.translate()
            }
            .sortedWith(compareBy({ it.hours }, { it.minutes }, { it.seconds }, { it.tenthsOfSecond }))
            .let { expectedAudioTranscriptions ->
                audioTranscriptionsCassandraDao.findBy(expectedAudioTranscriptions.first().sourceAudioFileName)
                    .map { it.translate() }
                    .toList()
                    .sortedWith(compareBy({ it.hours }, { it.minutes }, { it.seconds }, { it.tenthsOfSecond }))
                    .also { assertThat(it.size, Is(equalTo(expectedAudioTranscriptions.size))) }
                    .forEachIndexed { index, actualAudioTranscription ->
                        assertThat(actualAudioTranscription, Is(equalTo(expectedAudioTranscriptions[index])))
                    }
            }
    }

    @Test
    fun shouldThrowRecordNotFoundException() {
        val fakeAudioFileName = "any-audio-file-name"
        val fakeHours = 0
        val fakeMinutes = 0
        val fakeSeconds = 0
        val fakeTenths = 0
        val actualException = Assertions.assertThrows(PersistenceException::class.java) {
            audioTranscriptionsCassandraDao.findBy(fakeAudioFileName, fakeHours, fakeMinutes, fakeSeconds, fakeTenths)
        }
        val expectedException = PersistenceException.recordNotFoundInRepository(
            entityName = AudioTranscriptionCassandraRecord::class.java.simpleName,
            keys = mapOf(
                TranscriptionCassandraRecord.AUDIO_FILE_NAME_COLUMN to fakeAudioFileName,
                TranscriptionCassandraRecord.HOURS_COLUMN to fakeHours,
                TranscriptionCassandraRecord.MINUTES_COLUMN to fakeMinutes,
                TranscriptionCassandraRecord.SECONDS_COLUMN to fakeSeconds,
                TranscriptionCassandraRecord.TENTHS_COLUMN to fakeTenths))
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
    }
}
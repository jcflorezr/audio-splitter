package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audioclips.AudioClipsCassandraDaoTestDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
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
@ContextConfiguration(classes = [AudioClipsCassandraDaoTestDI::class])
internal class AudioClipsCassandraDaoTest {

    @Autowired
    private lateinit var audioClipsCassandraDao: AudioClipsCassandraDao
    @Autowired
    private lateinit var cassandraDaoTestDI: AudioClipsCassandraDaoTestDI

    private val thisClass: Class<AudioClipsCassandraDaoTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/audio-clips").path

    @PostConstruct
    fun setUp() {
        cassandraDaoTestDI.createTable(
            AudioClipCassandraRecord.TABLE_NAME, AudioClipCassandraRecord::class.java)
        cassandraDaoTestDI.createTable(
            ActiveSegmentCassandraRecord.TABLE_NAME, ActiveSegmentCassandraRecord::class.java)
    }

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    @Test
    fun saveAndRetrieveAudioClips() {
        val expectedAudioClipsFile = File("$testResourcesPath/test-audio-clips.json")
        val audioClipsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClip::class.java)
        val expectedAudioClips: List<AudioClip> = MAPPER.readValue(expectedAudioClipsFile, audioClipsListType)
        expectedAudioClips.forEach { expectedAudioClip ->
            audioClipsCassandraDao.save(AudioClipInfoCassandraRecord.fromEntity(expectedAudioClip))
            expectedAudioClip.run { audioClipsCassandraDao.findBy(sourceAudioFileName, hours, minutes, seconds, tenthsOfSecond) }
        }
        val audioFileName = expectedAudioClips.first().sourceAudioFileName
        val actualAudioClips = audioClipsCassandraDao.findBy(audioFileName).map { it.translate() }.toList()
        assertThat(actualAudioClips.size, Is(equalTo(expectedAudioClips.size)))
        assertThat(actualAudioClips, Is(equalTo(expectedAudioClips)))
    }

    @Test
    fun shouldThrowRecordNotFoundException() {
        val fakeAudioFileName = "any-audio-file-name"
        val fakeHours = 0
        val fakeMinutes = 0
        val fakeSeconds = 0
        val fakeTenths = 0
        val actualException = Assertions.assertThrows(PersistenceException::class.java) {
            audioClipsCassandraDao.findBy(fakeAudioFileName, fakeHours, fakeMinutes, fakeSeconds, fakeTenths)
        }
        val expectedException = PersistenceException.recordNotFoundInRepository(
            entityName = AudioClipInfoCassandraRecord::class.java.simpleName,
            keys = mapOf(
                AudioClipCassandraRecord.AUDIO_FILE_NAME_COLUMN to fakeAudioFileName,
                AudioClipCassandraRecord.HOURS_COLUMN to fakeHours,
                AudioClipCassandraRecord.MINUTES_COLUMN to fakeMinutes,
                AudioClipCassandraRecord.SECONDS_COLUMN to fakeSeconds,
                AudioClipCassandraRecord.TENTHS_COLUMN to fakeTenths))
        assertThat(actualException.errorCode, Is(equalTo(expectedException.errorCode)))
        assertThat(actualException.message, Is(equalTo(expectedException.message)))
    }
}
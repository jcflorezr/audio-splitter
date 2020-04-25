package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments.BasicAudioSegmentsCassandraDaoTestDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import javax.annotation.PostConstruct

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [BasicAudioSegmentsCassandraDaoTestDI::class])
internal class BasicAudioSegmentsCassandraDaoTest {

    @Autowired
    private lateinit var audioSegmentsCassandraDao: AudioSegmentsCassandraDao
    @Autowired
    private lateinit var basicAudioSegmentsCassandraDao: BasicAudioSegmentsCassandraDao
    @Autowired
    private lateinit var basicCassandraDaoTestDI: BasicAudioSegmentsCassandraDaoTestDI

    private val thisClass: Class<BasicAudioSegmentsCassandraDaoTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/audio-segments").path

    @PostConstruct
    fun setUp() {
        basicCassandraDaoTestDI.createTable(
            AudioSegmentCassandraRecord.TABLE_NAME, AudioSegmentCassandraRecord::class.java)
    }

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    @Test
    fun retrieveBasicAudioSegments() {
        val audioSegmentsToStoreFile = File("$testResourcesPath/basic-audio-segments/test-audio-segments.json")
        val expectedBasicAudioSegmentsFile = File("$testResourcesPath/basic-audio-segments/test-basic-audio-segments.json")

        val audioSegmentsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSegment::class.java)
        val audioSegmentsToStore: List<AudioSegment> = MAPPER.readValue(audioSegmentsToStoreFile, audioSegmentsListType)
        audioSegmentsToStore.forEach { expectedAudioSegment ->
            audioSegmentsCassandraDao.save(AudioSegmentCassandraRecord.fromEntity(expectedAudioSegment))
        }
        val audioFileName = audioSegmentsToStore.first().sourceAudioFileName
        val actualAudioSegments = basicAudioSegmentsCassandraDao.findBy(audioFileName).map { it.translate() }.toList()

        val basicAudioSegmentsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, BasicAudioSegment::class.java)
        val expectedAudioSegments: List<BasicAudioSegment> = MAPPER.readValue(expectedBasicAudioSegmentsFile, basicAudioSegmentsListType)

        assertThat(actualAudioSegments.size, Is(equalTo(expectedAudioSegments.size)))
        assertThat(actualAudioSegments, Is(equalTo(expectedAudioSegments)))
    }
}
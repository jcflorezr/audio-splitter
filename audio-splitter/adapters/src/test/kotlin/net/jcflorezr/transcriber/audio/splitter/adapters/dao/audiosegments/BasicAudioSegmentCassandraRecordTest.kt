package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class BasicAudioSegmentCassandraRecordTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<BasicAudioSegmentCassandraRecordTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/audio-segments/basic-audio-segments").path

    @Test
    fun fromEntityToRecord_And_FromRecordToEntity() {
        val expectedBasicAudioSegment =
            MAPPER.readValue(File("$testResourcesPath/test-single-basic-audio-segment.json"), BasicAudioSegment::class.java)
        val actualBasicAudioSegment = BasicAudioSegmentCassandraRecord.fromEntity(expectedBasicAudioSegment).translate()
        assertThat(actualBasicAudioSegment, Is(equalTo(expectedBasicAudioSegment)))
    }
}

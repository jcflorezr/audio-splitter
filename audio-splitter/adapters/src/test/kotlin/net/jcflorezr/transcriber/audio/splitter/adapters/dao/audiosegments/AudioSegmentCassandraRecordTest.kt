package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class AudioSegmentCassandraRecordTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioSegmentCassandraRecordTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/audio-segments").path

    @Test
    fun fromEntityToRecord_And_FromRecordToEntity() {
        val expectedAudioSegment =
            MAPPER.readValue(File("$testResourcesPath/test-single-audio-segment.json"), AudioSegment::class.java)
        val actualAudioSegment = AudioSegmentCassandraRecord.fromEntity(expectedAudioSegment).translate()
        assertThat(actualAudioSegment, Is(equalTo(expectedAudioSegment)))
    }
}

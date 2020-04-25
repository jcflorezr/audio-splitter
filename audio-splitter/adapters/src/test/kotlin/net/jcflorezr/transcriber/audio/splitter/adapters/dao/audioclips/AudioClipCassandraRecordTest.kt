package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

import java.io.File

internal class AudioClipCassandraRecordTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioClipCassandraRecordTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/audio-clips").path

    @Test
    fun fromEntityToRecord_And_FromRecordToEntity() {
        val expectedAudioClip =
            MAPPER.readValue(File("$testResourcesPath/test-single-audio-clip.json"), AudioClip::class.java)
        val actualAudioClip = AudioClipInfoCassandraRecord.fromEntity(expectedAudioClip).translate()
        assertThat(actualAudioClip, Is(equalTo(expectedAudioClip)))
    }
}
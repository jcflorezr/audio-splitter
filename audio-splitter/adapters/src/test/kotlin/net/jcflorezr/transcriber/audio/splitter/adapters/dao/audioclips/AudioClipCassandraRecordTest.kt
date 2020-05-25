package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Test

internal class AudioClipCassandraRecordTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val testResourcesPath: String = this.javaClass.getResource("/audio-clips").path

    @Test
    fun `from entity to record and then from record to entity`() {
        val expectedAudioClip =
            MAPPER.readValue(File("$testResourcesPath/test-single-audio-clip.json"), AudioClip::class.java)
        val actualAudioClip = AudioClipInfoCassandraRecord.fromEntity(expectedAudioClip).translate()
        assertThat(actualAudioClip, Is(equalTo(expectedAudioClip)))
    }
}

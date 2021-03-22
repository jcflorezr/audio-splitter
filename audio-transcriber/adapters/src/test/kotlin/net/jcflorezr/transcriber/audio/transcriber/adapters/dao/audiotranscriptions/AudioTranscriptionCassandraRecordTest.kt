package net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.io.FileNotFoundException
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class AudioTranscriptionCassandraRecordTest {

    private val mapper = ObjectMapper().registerKotlinModule()
    private val testResourcesPath = this.javaClass.getResource("/audio-clips-transcriptions").path

    @Test
    @DisplayName("from entity to record and then from record to entity")
    fun fromEntityToRecordAndBackToEntity() {
        val fileKeyword = "aggregate"
        val audioTranscriptionFile = File(testResourcesPath).listFiles()?.find { file -> file.name.contains(fileKeyword) }
            ?: throw FileNotFoundException("No suitable file found in '$testResourcesPath' to perform the tests")

        val expectedAudioTranscription = mapper.readValue(audioTranscriptionFile, AudioTranscription::class.java)
        val actualAudioTranscription =
            AudioTranscriptionCassandraRecord.fromEntity(expectedAudioTranscription).translate()
        assertThat(actualAudioTranscription, Is(equalTo(expectedAudioTranscription)))
    }
}

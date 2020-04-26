package net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.io.FileNotFoundException
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

internal class AudioTranscriptionCassandraRecordTest {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val thisClass: Class<AudioTranscriptionCassandraRecordTest> = this.javaClass
    private val testResourcesPath: String = thisClass.getResource("/audio-clips-transcriptions").path

    @Test
    fun fromEntityToRecord_And_FromRecordToEntity() {
        val fileKeyword = "aggregate"
        val audioTranscriptionFile = File(testResourcesPath).listFiles()?.find { file -> file.name.contains(fileKeyword) }
            ?: throw FileNotFoundException("No suitable file found in '$testResourcesPath' to perform the tests")

        val expectedAudioTranscription = MAPPER.readValue(audioTranscriptionFile, AudioTranscription::class.java)
        val actualAudioTranscription =
            AudioTranscriptionCassandraRecord.fromEntity(expectedAudioTranscription).translate()
        assertThat(actualAudioTranscription, Is(equalTo(expectedAudioTranscription)))
    }
}

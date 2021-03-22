package net.jcflorezr.transcriber.audio.transcriber.adapters.dao.audiotranscriptions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.io.File
import java.io.FileNotFoundException
import kotlinx.coroutines.runBlocking
import net.jcflorezr.transcriber.audio.transcriber.adapters.di.dao.audiotranscriptions.AudioTranscriptionsCassandraDaoIntTestDI
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.core.exception.FileException
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
internal class AudioTranscriptionsCassandraDaoIntTest {

    private val mapper = ObjectMapper().registerKotlinModule()
    private val testResourcesPath: String = this.javaClass.getResource("/audio-clips-transcriptions").path

    private lateinit var audioTranscriptionsCassandraDao: AudioTranscriptionsCassandraDao

    @BeforeAll
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(AudioTranscriptionsCassandraDaoIntTestDI, testContext.completing())
        audioTranscriptionsCassandraDao = AudioTranscriptionsCassandraDaoIntTestDI.audioTranscriptionsCassandraDao
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
        testContext.completeNow()
    }

    @Test
    @DisplayName("save audio transcriptions to db and then retrieve them from db")
    fun saveAudioTranscriptionsToDb(testContext: VertxTestContext) = runBlocking {
        val filesKeyword = "aggregate"
        val testDirectory = File(testResourcesPath).takeIf { it.exists() }
            ?: throw FileException.fileNotFound(testResourcesPath)
        val expectedAudioTranscriptionsFiles =
            testDirectory.listFiles { file -> file.nameWithoutExtension.contains(filesKeyword) }?.takeIf { it.isNotEmpty() }
                ?: throw FileNotFoundException("No files with keyword '$filesKeyword' were found in directory '$testResourcesPath'")
        expectedAudioTranscriptionsFiles
            .map { expectedAudioTranscriptionsFile ->
                mapper.readValue(expectedAudioTranscriptionsFile, AudioTranscription::class.java)
            }.onEach { expectedAudioTranscription ->
                audioTranscriptionsCassandraDao.save(AudioTranscriptionCassandraRecord.fromEntity(expectedAudioTranscription))
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
        testContext.completeNow()
    }
}

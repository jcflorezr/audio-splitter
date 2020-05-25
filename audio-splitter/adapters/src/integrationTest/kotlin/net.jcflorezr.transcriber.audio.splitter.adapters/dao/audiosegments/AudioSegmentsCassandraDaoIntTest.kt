package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.io.File
import java.io.FileNotFoundException
import kotlinx.coroutines.runBlocking
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments.AudioSegmentsCassandraDaoIntTestDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.core.exception.FileException
import net.jcflorezr.transcriber.core.util.JsonUtils.fromJsonToList
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
internal class AudioSegmentsCassandraDaoIntTest {

    private val testResourcesPath: String = this.javaClass.getResource("/audio-segments").path
    private val audioSegmentsCassandraDaoIntTestDI = AudioSegmentsCassandraDaoIntTestDI
    private lateinit var audioSegmentsCassandraDao: AudioSegmentsCassandraDao

    @BeforeAll
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(audioSegmentsCassandraDaoIntTestDI, testContext.completing())
        audioSegmentsCassandraDao = audioSegmentsCassandraDaoIntTestDI.audioSegmentsCassandraDao
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
        audioSegmentsCassandraDaoIntTestDI.cassandraClient.close()
        testContext.completeNow()
    }

    @Test
    fun `save audio segments to db and then retrieve them from db`(testContext: VertxTestContext) = runBlocking {
        val testDirectory = File(testResourcesPath).takeIf { it.exists() } ?: throw FileException.fileNotFound(testResourcesPath)
        val expectedAudioSegmentsFiles =
            testDirectory
                .listFiles { file -> !file.isDirectory }
                ?.takeIf { it.isNotEmpty() }
            ?: throw FileNotFoundException("No expected files were found in directory '$testResourcesPath'")

        expectedAudioSegmentsFiles
            .flatMap { fromJsonToList<AudioSegment>(jsonFile = it) }
            .onEach { audioSegmentsCassandraDao.save(AudioSegmentCassandraRecord.fromEntity(audioSegment = it)) }
            .let { expectedAudioSegments ->
                val audioFileName = expectedAudioSegments.first().sourceAudioFileName
                val segmentStartInSeconds = expectedAudioSegments.first().segmentStartInSeconds
                val segmentEndInSeconds = expectedAudioSegments.last().segmentStartInSeconds
                val actualAudioSegments =
                    audioSegmentsCassandraDao.findRange(audioFileName, segmentStartInSeconds, segmentEndInSeconds)
                        .map { it.translate() }
                assertThat(actualAudioSegments.size, Is(equalTo(expectedAudioSegments.size)))
                assertThat(actualAudioSegments, Is(equalTo(expectedAudioSegments)))
            }
        testContext.completeNow()
    }
}

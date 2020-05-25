package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audiosegments

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.io.File
import kotlinx.coroutines.runBlocking
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audiosegments.AudioSegmentsCassandraDaoIntTestDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.core.util.JsonUtils.fromJsonToList
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
internal class BasicAudioSegmentsCassandraDaoIntTest {

    private val testResourcesPath: String = this.javaClass.getResource("/audio-segments").path
    private val audioSegmentsCassandraDaoIntTestDI = AudioSegmentsCassandraDaoIntTestDI
    private lateinit var audioSegmentsCassandraDao: AudioSegmentsCassandraDao
    private lateinit var basicAudioSegmentsCassandraDao: BasicAudioSegmentsCassandraDao

    @BeforeAll
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(audioSegmentsCassandraDaoIntTestDI, testContext.completing())
        audioSegmentsCassandraDao = audioSegmentsCassandraDaoIntTestDI.audioSegmentsCassandraDao
        basicAudioSegmentsCassandraDao = audioSegmentsCassandraDaoIntTestDI.basicAudioSegmentsCassandraDao
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
        audioSegmentsCassandraDaoIntTestDI.cassandraClient.close()
        testContext.completeNow()
    }

    @Test
    fun `save audio segments to db and then retrieve them from db as basic audio segments`(
        testContext: VertxTestContext
    ) = runBlocking {
        fromJsonToList<AudioSegment>(jsonFile = File("$testResourcesPath/basic-audio-segments/test-audio-segments.json"))
            .onEach { audioSegmentToStore ->
                audioSegmentsCassandraDao.save(AudioSegmentCassandraRecord.fromEntity(audioSegmentToStore))
            }
            .let { storedAudioSegments ->
                val audioFileName = storedAudioSegments.first().sourceAudioFileName
                val actualBasicAudioSegments = basicAudioSegmentsCassandraDao.findBy(audioFileName).map { it.translate() }
                val expectedBasicAudioSegments = fromJsonToList<BasicAudioSegment>(
                    jsonFile = File("$testResourcesPath/basic-audio-segments/test-basic-audio-segments.json")
                )
                assertThat(actualBasicAudioSegments.size, Is(equalTo(expectedBasicAudioSegments.size)))
                assertThat(actualBasicAudioSegments, Is(equalTo(expectedBasicAudioSegments)))
            }
        testContext.completeNow()
    }
}

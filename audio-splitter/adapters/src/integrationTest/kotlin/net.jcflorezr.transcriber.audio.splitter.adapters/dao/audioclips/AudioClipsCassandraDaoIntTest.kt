package net.jcflorezr.transcriber.audio.splitter.adapters.dao.audioclips

import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import java.io.File
import kotlinx.coroutines.runBlocking
import net.jcflorezr.transcriber.audio.splitter.adapters.di.dao.audioclips.AudioClipsCassandraDaoIntTestDI
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.core.util.JsonUtils.fromJsonToList
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
internal class AudioClipsCassandraDaoIntTest {

    private val testResourcesPath: String = this.javaClass.getResource("/audio-clips").path

    private val audioClipsCassandraDao = AudioClipsCassandraDaoIntTestDI.audioClipsCassandraDao

    @BeforeAll
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(AudioClipsCassandraDaoIntTestDI, testContext.completing())
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
        testContext.completeNow()
    }

    @Test
    @DisplayName("save audio clips to db and then retrieve them from db")
    fun saveAudioClipsToDb(testContext: VertxTestContext) = runBlocking {
        val expectedAudioClips = fromJsonToList<AudioClip>(jsonFile = File("$testResourcesPath/test-audio-clips.json"))
        expectedAudioClips.forEach { expectedAudioClip ->
            audioClipsCassandraDao.save(AudioClipInfoCassandraRecord.fromEntity(expectedAudioClip))
        }
        val audioFileName = expectedAudioClips.first().sourceAudioFileName
        val actualAudioClips = audioClipsCassandraDao.findBy(audioFileName).map { it.translate() }.toList()
        assertThat(actualAudioClips.size, Is(equalTo(expectedAudioClips.size)))
        assertThat(actualAudioClips, Is(equalTo(expectedAudioClips)))
        testContext.completeNow()
    }
}

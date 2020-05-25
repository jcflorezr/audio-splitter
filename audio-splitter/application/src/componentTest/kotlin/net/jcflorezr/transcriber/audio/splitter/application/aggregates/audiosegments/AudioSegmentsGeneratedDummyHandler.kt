package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments

import io.vertx.junit5.VertxTestContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.audiosegments.DefaultAudioSegmentsRepository
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegments
import net.jcflorezr.transcriber.audio.splitter.domain.events.audiosegments.AudioSegmentsGenerated
import net.jcflorezr.transcriber.core.config.util.TestUtils.waitAtMostTenSecondsByOneSecondIntervals
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Event
import net.jcflorezr.transcriber.core.domain.EventHandler
import net.jcflorezr.transcriber.core.domain.EventRouter
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is

@ObsoleteCoroutinesApi
class AudioSegmentsGeneratedDummyHandler(
    private val audioSegmentsRepository: DefaultAudioSegmentsRepository
) : EventHandler<Event<AggregateRoot>> {

    private val audioSegmentsActor = AudioSegmentsActor()
    private val audioSegmentsGenerated = mutableMapOf<String, List<BasicAudioSegment>>()

    init {
        EventRouter.register(AudioSegmentsGenerated::class.java, this)
    }

    override suspend fun execute(event: Event<AggregateRoot>) {
        val audioSegmentsGenerated = event as AudioSegmentsGenerated
        audioSegmentsActor.mainActor.send(StoreAudioSegmentsReceived(audioSegmentsGenerated.audioSegments))
    }

    suspend fun assertAudioSegments(audioFileName: String, testContext: VertxTestContext) = withContext(Dispatchers.IO) {
        waitAtMostTenSecondsByOneSecondIntervals().until { audioSegmentsGenerated.containsKey(audioFileName) }
        val actualAudioSegments = audioSegmentsGenerated[audioFileName]
            ?: throw IllegalStateException("Probably actual audio segments were deleted during the assertion process")
        val expectedAudioSegments = audioSegmentsRepository.findBasicSegmentsBy(audioFileName)
        assertThat(actualAudioSegments.size, Is(equalTo(expectedAudioSegments.size)))
        waitAtMostTenSecondsByOneSecondIntervals().untilAsserted {
            actualAudioSegments
                .sortedBy { it.segmentStart }
                .forEachIndexed { index, actualAudioSegment ->
                    assertThat(actualAudioSegment, Is(equalTo(expectedAudioSegments[index])))
                }
        }
        testContext.completeNow()
    }

    private inner class AudioSegmentsActor {

        val mainActor: SendChannel<AudioSegmentReceivedMsg> =
            CoroutineScope(Dispatchers.Default).audioSegmentsActor()

        private fun CoroutineScope.audioSegmentsActor() = actor<AudioSegmentReceivedMsg> {
            for (msg in channel) {
                when (msg) {
                    is StoreAudioSegmentsReceived -> {
                        val basicAudioSegments = msg.audioSegmentsGenerated.basicAudioSegments
                        val audioFileName = basicAudioSegments.first().sourceAudioFileName
                        audioSegmentsGenerated[audioFileName] = basicAudioSegments
                    }
                }
            }
        }
    }
}

/*
    Family classes for actors which process the Audio Transcriptions received through events
 */

sealed class AudioSegmentReceivedMsg
data class StoreAudioSegmentsReceived(val audioSegmentsGenerated: BasicAudioSegments) : AudioSegmentReceivedMsg()

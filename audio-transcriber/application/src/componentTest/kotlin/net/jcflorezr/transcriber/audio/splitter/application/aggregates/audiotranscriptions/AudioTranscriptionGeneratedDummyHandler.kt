package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions

import io.vertx.junit5.VertxTestContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import net.jcflorezr.transcriber.audio.transcriber.adapters.repositories.audiotranscriptions.DefaultAudioTranscriptionsRepository
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.audio.transcriber.domain.events.audiotranscriptions.AudioTranscriptionGenerated
import net.jcflorezr.transcriber.core.config.util.TestUtils.waitAtMostTenSecondsByOneSecondIntervals
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Event
import net.jcflorezr.transcriber.core.domain.EventHandler
import net.jcflorezr.transcriber.core.domain.EventRouter
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is

@ObsoleteCoroutinesApi
class AudioTranscriptionGeneratedDummyHandler(
    private val audioTranscriptionsRepository: DefaultAudioTranscriptionsRepository
) : EventHandler<Event<AggregateRoot>> {

    private val audioTranscriptionsActor = AudioTranscriptionsActor()
    private val expectedAudioTranscriptions = mutableListOf<AudioTranscription>()
    private val thisClass: Class<AudioTranscriptionGeneratedDummyHandler> = this.javaClass
    private val transcriptionsFilesPath: String

    init {
        transcriptionsFilesPath = thisClass.getResource("/audio-clips-transcriptions").path
        EventRouter.register(AudioTranscriptionGenerated::class.java, this)
    }

    override suspend fun execute(event: Event<AggregateRoot>) {
        val audioTranscriptionGenerated = event as AudioTranscriptionGenerated
        audioTranscriptionsActor.mainActor.send(StoreAudioTranscriptionGenerated(audioTranscriptionGenerated))
    }

    suspend fun assertAudioTranscriptions(sourceAudioFileName: String, vertxTestContext: VertxTestContext) {
        val actualAudioTranscriptions = audioTranscriptionsRepository.findBy(sourceAudioFileName)
        // Wait up to 10 seconds in 1 second intervals until all the expectedAudioTranscriptions have arrived
        waitAtMostTenSecondsByOneSecondIntervals().untilAsserted {
            assertThat(
                getMissingExpectedAudioTranscriptionsErrorMessage(actualAudioTranscriptions),
                actualAudioTranscriptions.size, Is(equalTo(expectedAudioTranscriptions.size)))
        }
        expectedAudioTranscriptions
            .sortedWith(compareBy({ it.hours }, { it.minutes }, { it.seconds }, { it.tenthsOfSecond }))
            .forEachIndexed { index, expectedAudioTranscription ->
                assertThat(actualAudioTranscriptions[index], Is(equalTo(expectedAudioTranscription)))
            }
        vertxTestContext.completeNow()
    }

    private fun getMissingExpectedAudioTranscriptionsErrorMessage(
        expectedAudioTranscriptions: List<AudioTranscription>
    ): String {
        val differencesList = expectedAudioTranscriptions - this.expectedAudioTranscriptions
        return "There were ${differencesList.size} audio transcriptions that were not generated. \n" +
            "transcriptions locations: " +
            "${differencesList.map { "${it.hours} - ${it.minutes} - ${it.seconds} - ${it.tenthsOfSecond}" }}"
    }

    private inner class AudioTranscriptionsActor {

        val mainActor: SendChannel<AudioTranscriptionGeneratedMsg> =
            CoroutineScope(Dispatchers.Default).audioTranscriptionsActor()

        private fun CoroutineScope.audioTranscriptionsActor() = actor<AudioTranscriptionGeneratedMsg> {
            for (msg in channel) {
                when (msg) {
                    is StoreAudioTranscriptionGenerated ->
                        expectedAudioTranscriptions.add(msg.audioTranscriptionGenerated.audioTranscription)
                }
            }
        }
    }
}

/*
    Family classes for actors which process the Audio Transcriptions received through events
 */

sealed class AudioTranscriptionGeneratedMsg
data class StoreAudioTranscriptionGenerated(val audioTranscriptionGenerated: AudioTranscriptionGenerated) : AudioTranscriptionGeneratedMsg()

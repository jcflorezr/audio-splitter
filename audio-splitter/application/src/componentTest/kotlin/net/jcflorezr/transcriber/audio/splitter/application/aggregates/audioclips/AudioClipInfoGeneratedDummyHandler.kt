package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import io.vertx.junit5.VertxTestContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.audioclips.DefaultAudioClipsRepository
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.audio.splitter.domain.events.audioclips.AudioClipInfoGenerated
import net.jcflorezr.transcriber.core.config.util.TestUtils.waitAtMostTenSecondsByOneSecondIntervals
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Event
import net.jcflorezr.transcriber.core.domain.EventHandler
import net.jcflorezr.transcriber.core.domain.EventRouter
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is

@ObsoleteCoroutinesApi
class AudioClipInfoGeneratedDummyHandler(
    private val audioClipsRepository: DefaultAudioClipsRepository
) : EventHandler<Event<AggregateRoot>> {

    private val audioClipInfoActor = AudioClipInfoActor()
    private val audioClipsInfoGenerated = mutableMapOf<String, MutableList<AudioClip>>()

    init {
        EventRouter.register(AudioClipInfoGenerated::class.java, this)
    }

    override suspend fun execute(event: Event<AggregateRoot>) {
        val audioClipInfoGenerated = event as AudioClipInfoGenerated
        audioClipInfoActor.mainActor.send(StoreAudioClipInfoReceived(audioClipInfoGenerated.audioClip))
    }

    suspend fun assertAudioClips(audioFileName: String, testContext: VertxTestContext) = withContext(Dispatchers.IO) {
        waitAtMostTenSecondsByOneSecondIntervals().until { audioClipsInfoGenerated.containsKey(audioFileName) }
        val actualAudioClipsInfo = audioClipsInfoGenerated[audioFileName]
            ?: throw IllegalStateException("Probably actual audio clips info were deleted during the assertion process")
        val expectedAudioClipsInfo = audioClipsRepository.findBy(audioFileName)
        assertThat(actualAudioClipsInfo.size, Is(equalTo(expectedAudioClipsInfo.size)))
        waitAtMostTenSecondsByOneSecondIntervals().untilAsserted {
            actualAudioClipsInfo
                .sortedWith(compareBy({ it.hours }, { it.minutes }, { it.seconds }, { it.tenthsOfSecond }))
                .forEachIndexed { index, actualAudioClip ->
                    assertThat(actualAudioClip, Is(equalTo(expectedAudioClipsInfo[index])))
                }
        }
        testContext.completeNow()
    }

    private inner class AudioClipInfoActor {

        val mainActor: SendChannel<AudioClipInfoReceivedMsg> =
            CoroutineScope(Dispatchers.Default).activeSegmentsActor()

        private fun CoroutineScope.activeSegmentsActor() = actor<AudioClipInfoReceivedMsg> {
            for (msg in channel) {
                when (msg) {
                    is StoreAudioClipInfoReceived -> {
                        val audioFileName = msg.audioClip.sourceAudioFileName
                        audioClipsInfoGenerated.getOrPut(key = audioFileName) { mutableListOf() }
                            .also { audioClipsInfoList -> audioClipsInfoList.add(msg.audioClip) }
                    }
                }
            }
        }
    }
}

/*
    Family classes for actors which process the Audio Clips Info received through events
 */

sealed class AudioClipInfoReceivedMsg
data class StoreAudioClipInfoReceived(val audioClip: AudioClip) : AudioClipInfoReceivedMsg()

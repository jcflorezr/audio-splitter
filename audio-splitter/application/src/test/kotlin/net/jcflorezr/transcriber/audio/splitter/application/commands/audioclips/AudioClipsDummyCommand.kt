package net.jcflorezr.transcriber.audio.splitter.application.commands.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.core.domain.Command
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import java.io.File

sealed class ActiveSegmentReceivedMsg
data class StoreActiveSegmentReceived(val audioClip: AudioClip) : ActiveSegmentReceivedMsg()
object AssertActiveSegmentReceived : ActiveSegmentReceivedMsg()

@ObsoleteCoroutinesApi
class AudioClipsDummyCommand : Command<AudioClip> {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val activeSegmentsActor : SendChannel<ActiveSegmentReceivedMsg>
        = CoroutineScope(Dispatchers.Default).activeSegmentsActor()

    private val actualAudioClips = mutableListOf<AudioClip>()
    private val thisClass: Class<AudioClipsDummyCommand> = this.javaClass
    private val sourceFilesPath: String

    init {
        sourceFilesPath = thisClass.getResource("/audio-clips").path
    }

    override suspend fun execute(aggregateRoot: AudioClip) {
        activeSegmentsActor.send(StoreActiveSegmentReceived(aggregateRoot))
    }

    private fun CoroutineScope.activeSegmentsActor() = actor<ActiveSegmentReceivedMsg> {
        for (msg in channel) {
            when (msg) {
                is StoreActiveSegmentReceived -> actualAudioClips.add(msg.audioClip)
                is AssertActiveSegmentReceived -> assertActiveSegmentsReceived()
            }
        }
    }

    private suspend fun assertActiveSegmentsReceived() = activeSegmentsActor.send(AssertActiveSegmentReceived)

    suspend fun assertAudioClips() = withContext(Dispatchers.IO) {
        val audioFileName = File(actualAudioClips[0].sourceAudioFileName).nameWithoutExtension
        val audioClipsPath = "$sourceFilesPath/$audioFileName-audio-clips.json"
        val audioClipsListType =
            MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClip::class.java)
        val expectedAudioClips: List<AudioClip> =
            MAPPER.readValue(File(audioClipsPath), audioClipsListType)

        assertThat(getMissingExpectedAudioClipsErrorMessage(expectedAudioClips),
            actualAudioClips.size, Is(equalTo(expectedAudioClips.size)))
        actualAudioClips.sortedWith(compareBy( { it.hours }, { it.minutes }, { it.seconds }, { it.tenthsOfSecond }) )
        .forEachIndexed { index, actualAudioClip ->
            assertThat(actualAudioClip, Is(equalTo(expectedAudioClips[index])))
        }
    }

    private fun getMissingExpectedAudioClipsErrorMessage(expectedAudioClips: List<AudioClip>): String {
        val differencesList = expectedAudioClips - actualAudioClips
        return "There were ${differencesList.size} audio clips that were not generated. \n" +
            "clips locations in seconds: ${differencesList.map { it.audioClipFileName }}"
    }
}
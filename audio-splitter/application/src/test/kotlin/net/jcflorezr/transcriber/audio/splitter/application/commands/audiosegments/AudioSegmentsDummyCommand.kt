package net.jcflorezr.transcriber.audio.splitter.application.commands.audiosegments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.core.domain.Command
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat

sealed class AudioSegmentReceivedMsg
data class StoreAudioSegmentReceived(val audioSegment: AudioSegment) : AudioSegmentReceivedMsg()
object AssertAudioSegmentsReceived : AudioSegmentReceivedMsg()

@ObsoleteCoroutinesApi
class AudioSegmentsDummyCommand : Command<AudioSegment> {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val audioSegmentsActor: SendChannel<AudioSegmentReceivedMsg> =
        CoroutineScope(Dispatchers.Default).audioSegmentsActor()

    private val actualAudioSegments = mutableListOf<AudioSegment>()
    private val thisClass: Class<AudioSegmentsDummyCommand> = this.javaClass
    private val sourceFilesPath: String

    init {
        sourceFilesPath = thisClass.getResource("/audio-segments").path
    }

    override suspend fun execute(aggregateRoot: AudioSegment) {
        audioSegmentsActor.send(StoreAudioSegmentReceived(aggregateRoot))
    }

    private fun CoroutineScope.audioSegmentsActor() = actor<AudioSegmentReceivedMsg> {
        for (msg in channel) {
            when (msg) {
                is StoreAudioSegmentReceived -> actualAudioSegments.add(msg.audioSegment)
                is AssertAudioSegmentsReceived -> assertAudioSegmentsReceived()
            }
        }
    }

    private suspend fun assertAudioSegmentsReceived() = audioSegmentsActor.send(AssertAudioSegmentsReceived)

    suspend fun assertAudioSegments() = withContext(Dispatchers.IO) {
        val audioFileName = File(actualAudioSegments[0].sourceAudioFileName).nameWithoutExtension
        val audioSegmentsPath = "$sourceFilesPath/$audioFileName-audio-segments.json"

        val audioSegmentsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSegment::class.java)
        val expectedAudioSegments: List<AudioSegment> = MAPPER.readValue(File(audioSegmentsPath), audioSegmentsListType)

        assertThat(getMissingExpectedAudioSegmentsErrorMessage(expectedAudioSegments),
            actualAudioSegments.size, Is(equalTo(expectedAudioSegments.size)))
        actualAudioSegments.sortedBy { it.segmentStart }.forEachIndexed { index, actualAudioSegment ->
            assertThat(actualAudioSegment, Is(equalTo(expectedAudioSegments[index])))
        }
    }

    private fun getMissingExpectedAudioSegmentsErrorMessage(expectedAudioSegments: List<AudioSegment>): String {
        val differencesList = expectedAudioSegments - actualAudioSegments
        return "There were ${differencesList.size} audio segments that were not generated. \n" +
            "Segments locations in seconds: ${differencesList.map { it.segmentStartInSeconds }}"
    }
}

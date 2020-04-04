package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Command
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import java.io.File
import org.hamcrest.CoreMatchers.`is` as Is

sealed class AudioSegmentReceivedMsg
data class StoreAudioSegmentReceived(val audioSegment: AudioSegment) : AudioSegmentReceivedMsg()
object AssertAudioSegmentsReceived : AudioSegmentReceivedMsg()

@ObsoleteCoroutinesApi
class AudioSegmentsDummyCommand : Command {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val audioSegmentsActor : SendChannel<AudioSegmentReceivedMsg>
        = CoroutineScope(Dispatchers.Default).audioSegmentsActor()

    private val actualAudioSegments = mutableListOf<AudioSegment>()
    private val thisClass: Class<AudioSegmentsDummyCommand> = this.javaClass
    private val sourceFilesPath: String

    init {
        sourceFilesPath = thisClass.getResource("/audio-segments").path
    }

    override suspend fun execute(aggregateRoot: AggregateRoot) {
        val audioSegment = aggregateRoot as AudioSegment
        audioSegmentsActor.send(StoreAudioSegmentReceived(audioSegment))
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
        val audioFileName = File(actualAudioSegments[0].audioFileName).nameWithoutExtension
        val audioSegmentsPath = "$sourceFilesPath/$audioFileName-audio-segments.json"

        val audioSegmentsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioSegment::class.java)
        val expectedAudioSegments: List<AudioSegment> = MAPPER.readValue(File(audioSegmentsPath), audioSegmentsListType)

        assertThat(getMissingExpectedAudioSegmentsErrorMessage(expectedAudioSegments),
            actualAudioSegments.size, Is(equalTo(expectedAudioSegments.size)))
        assertThat(actualAudioSegments.sortedBy { it.segmentStart }, Is(equalTo(expectedAudioSegments)))
    }

    private fun getMissingExpectedAudioSegmentsErrorMessage(expectedAudioSegments: List<AudioSegment>): String {
        val differencesList = expectedAudioSegments - actualAudioSegments
        return "There were ${differencesList.size} audio segments that were not generated. \n" +
            "Segments locations in seconds: ${differencesList.map { it.segmentStartInSeconds }}"
    }
}
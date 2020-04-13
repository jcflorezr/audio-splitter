package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiotranscriptions

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.io.FileNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Command
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.assertThat

sealed class AudioTranscriptionReceivedMsg
data class StoreAudioTranscriptionReceived(val audioTranscription: AudioTranscription) : AudioTranscriptionReceivedMsg()
object AssertAudioTranscriptionReceived : AudioTranscriptionReceivedMsg()

@ObsoleteCoroutinesApi
class AudioTranscriptionDummyCommand : Command {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val activeSegmentsActor : SendChannel<AudioTranscriptionReceivedMsg>
        = CoroutineScope(Dispatchers.Default).activeSegmentsActor()

    private val actualAudioTranscriptions = mutableListOf<AudioTranscription>()
    private val thisClass: Class<AudioTranscriptionDummyCommand> = this.javaClass
    private val transcriptionsFilesPath: String

    init {
        transcriptionsFilesPath = thisClass.getResource("/audio-clips-transcriptions").path
    }

    override suspend fun execute(aggregateRoot: AggregateRoot) {
        val audioClip = aggregateRoot as AudioTranscription
        activeSegmentsActor.send(StoreAudioTranscriptionReceived(audioClip))
    }

    private fun CoroutineScope.activeSegmentsActor() = actor<AudioTranscriptionReceivedMsg> {
        for (msg in channel) {
            when (msg) {
                is StoreAudioTranscriptionReceived -> actualAudioTranscriptions.add(msg.audioTranscription)
                is AssertAudioTranscriptionReceived -> assertActiveSegmentsReceived()
            }
        }
    }

    private suspend fun assertActiveSegmentsReceived() = activeSegmentsActor.send(AssertAudioTranscriptionReceived)

    suspend fun assertAudioTranscriptions() = withContext(Dispatchers.IO) {

        val expectedAudioTranscriptions = File(transcriptionsFilesPath)
            .takeIf { it.exists() }
            ?.listFiles { file -> file.nameWithoutExtension.contains("aggregate") }
            ?.map { transcriptionFile -> MAPPER.readValue(transcriptionFile, AudioTranscription::class.java) }
            ?.sortedWith(compareBy( { it.hours }, { it.minutes }, { it.seconds }, { it.tenthsOfSecond }) )
            ?: throw FileNotFoundException("Directory '$transcriptionsFilesPath' was not found")

        assertThat(getMissingExpectedAudioTranscriptionsErrorMessage(expectedAudioTranscriptions),
            actualAudioTranscriptions.size, Is(equalTo(expectedAudioTranscriptions.size)))
        assertThat(
            actualAudioTranscriptions
                .sortedWith(compareBy( { it.hours }, { it.minutes }, { it.seconds }, { it.tenthsOfSecond }) ),
            Is(equalTo(expectedAudioTranscriptions)))
    }

    private fun getMissingExpectedAudioTranscriptionsErrorMessage(
        expectedAudioTranscriptions: List<AudioTranscription>
    ): String {
        val differencesList = expectedAudioTranscriptions - actualAudioTranscriptions
        return "There were ${differencesList.size} audio transcriptions that were not generated. \n" +
            "transcriptions locations: " +
            "${differencesList.map { "${it.hours} - ${it.minutes} - ${it.seconds} - ${it.tenthsOfSecond}" }}"
    }
}
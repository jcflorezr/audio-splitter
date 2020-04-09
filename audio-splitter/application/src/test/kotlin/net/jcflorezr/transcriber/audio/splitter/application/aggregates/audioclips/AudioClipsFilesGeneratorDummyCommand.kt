package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.adapters.util.SupportedAudioFormats
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Command
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import java.io.FileInputStream

sealed class AudioClipFileGeneratedMsg
data class StoreAudioClipFileGenerated(val audioClip: AudioClip) : AudioClipFileGeneratedMsg()
object AssertAudioClipFileGenerated : AudioClipFileGeneratedMsg()

@ObsoleteCoroutinesApi
class AudioClipsFilesGeneratorDummyCommand(
    private val tempLocalDirectory: String
) : Command {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
        private val FLAC_EXTENSION = SupportedAudioFormats.FLAC.extension
    }

    private val activeSegmentsActor : SendChannel<AudioClipFileGeneratedMsg>
        = CoroutineScope(Dispatchers.Default).activeSegmentsActor()

    private val actualAudioClips = mutableListOf<AudioClip>()
    private val thisClass: Class<AudioClipsFilesGeneratorDummyCommand> = this.javaClass
    private val sourceFilesPath: String

    init {
        sourceFilesPath = thisClass.getResource("/audio-clips").path
    }

    override suspend fun execute(aggregateRoot: AggregateRoot) {
        val audioClip = aggregateRoot as AudioClip
        activeSegmentsActor.send(StoreAudioClipFileGenerated(audioClip))
    }

    private fun CoroutineScope.activeSegmentsActor() = actor<AudioClipFileGeneratedMsg> {
        for (msg in channel) {
            when (msg) {
                is StoreAudioClipFileGenerated -> actualAudioClips.add(msg.audioClip)
                is AssertAudioClipFileGenerated -> assertAudioClipFileGenerated()
            }
        }
    }

    private suspend fun assertAudioClipFileGenerated() = activeSegmentsActor.send(AssertAudioClipFileGenerated)

    suspend fun assertAudioClipsGenerated() = withContext(Dispatchers.IO) {
        assertAudioClipsWereProcessed()
        assertAudioClipsFiles()
    }

    private fun assertAudioClipsFiles() {
        try {
            actualAudioClips.forEach { audioClipInfo ->
                val firstSegment = audioClipInfo.activeSegments.first()
                val sourceAudioFileName = firstSegment.sourceAudioFileName
                val audioClipName = audioClipInfo.audioClipFileName()
                val actualAudioClipFile =
                    File("$tempLocalDirectory/audio-clips/$sourceAudioFileName/$audioClipName.$FLAC_EXTENSION")
                val expectedAudioClipFile =
                    File("$sourceFilesPath/audio-clips-files/$sourceAudioFileName/$audioClipName.$FLAC_EXTENSION")
                assertTrue(
                    IOUtils.contentEquals(FileInputStream(actualAudioClipFile), FileInputStream(expectedAudioClipFile)))
            }
        } finally {
            File("$tempLocalDirectory/audio-clips").deleteRecursively()
        }
    }

    private fun assertAudioClipsWereProcessed() {
        val audioFileName = File(actualAudioClips[0].activeSegments[0].sourceAudioFileName).nameWithoutExtension
        val audioClipsPath = "$sourceFilesPath/$audioFileName-audio-clips.json"
        val audioClipsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClip::class.java)
        val expectedAudioClips: List<AudioClip> = MAPPER.readValue(File(audioClipsPath), audioClipsListType)

        assertThat(getMissingExpectedAudioClipsErrorMessage(expectedAudioClips),
            actualAudioClips.size, Is(equalTo(expectedAudioClips.size)))
        assertThat(
            actualAudioClips.
                sortedWith(compareBy( { it.hours }, { it.minutes }, { it.seconds }, { it.tenthsOfSecond }) ),
            Is(equalTo(expectedAudioClips)))
    }

    private fun getMissingExpectedAudioClipsErrorMessage(expectedAudioClips: List<AudioClip>): String {
        val differencesList = expectedAudioClips - actualAudioClips
        return "There were ${differencesList.size} audio clips files that were not generated. \n" +
            "clips locations in seconds: ${differencesList.map { it.activeSegments[0].segmentStart }}"
    }
}
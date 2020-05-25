package net.jcflorezr.transcriber.audio.splitter.application.commands.audioclips

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Assertions.assertTrue

sealed class AudioClipFileGeneratedMsg
data class StoreAudioClipFileGenerated(val audioClipFileInfo: AudioClipFileInfo) : AudioClipFileGeneratedMsg()

@ObsoleteCoroutinesApi
class AudioClipsFilesGeneratorDummyCommand(
    private val tempLocalDirectory: String
) : Command<AudioClipFileInfo> {

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val activeSegmentsActor: SendChannel<AudioClipFileGeneratedMsg> =
        CoroutineScope(Dispatchers.Default).activeSegmentsActor()

    private val actualAudioClipsFilesInfo = mutableListOf<AudioClipFileInfo>()
    private val thisClass: Class<AudioClipsFilesGeneratorDummyCommand> = this.javaClass
    private val sourceFilesPath: String

    init {
        sourceFilesPath = thisClass.getResource("/audio-clips/audio-clips-files").path
    }

    override suspend fun execute(aggregateRoot: AudioClipFileInfo) {
        activeSegmentsActor.send(StoreAudioClipFileGenerated(aggregateRoot))
    }

    private fun CoroutineScope.activeSegmentsActor() = actor<AudioClipFileGeneratedMsg> {
        for (msg in channel) {
            when (msg) {
                is StoreAudioClipFileGenerated -> actualAudioClipsFilesInfo.add(msg.audioClipFileInfo)
            }
        }
    }

    suspend fun assertAudioClipsGenerated() = withContext(Dispatchers.IO) {
        assertProcessedAudioClipsFiles()
        assertAudioClipsFiles()
    }

    private fun assertAudioClipsFiles() {
        try {
            actualAudioClipsFilesInfo.forEach { audioClipInfo ->
                val actualAudioClipFile = audioClipInfo.run {
                    File("$tempLocalDirectory/$sourceAudioFileName/$audioClipFileName.$audioClipFileExtension") }
                val expectedAudioClipFile = audioClipInfo.run {
                    File("$sourceFilesPath/$sourceAudioFileName/$audioClipFileName.$audioClipFileExtension") }
                assertTrue(
                    IOUtils.contentEquals(FileInputStream(actualAudioClipFile), FileInputStream(expectedAudioClipFile)))
            }
        } finally {
            File("$tempLocalDirectory/${actualAudioClipsFilesInfo[0].sourceAudioFileName}").deleteRecursively()
        }
    }

    private fun assertProcessedAudioClipsFiles() {
        val audioFileName = File(actualAudioClipsFilesInfo[0].sourceAudioFileName).nameWithoutExtension
        val audioClipsPath = "$sourceFilesPath/$audioFileName-audio-clips-files-info.json"
        val audioClipsListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipFileInfo::class.java)
        val expectedAudioClipsFilesInfo: List<AudioClipFileInfo> = MAPPER.readValue(File(audioClipsPath), audioClipsListType)

        assertThat(getMissingExpectedAudioClipsErrorMessage(expectedAudioClipsFilesInfo),
            actualAudioClipsFilesInfo.size, Is(equalTo(expectedAudioClipsFilesInfo.size)))
        actualAudioClipsFilesInfo.sortedWith(compareBy({ it.hours }, { it.minutes }, { it.seconds }, { it.tenthsOfSecond }))
        .forEachIndexed { index, actualAudioClipFileInfo ->
            assertThat(actualAudioClipFileInfo, Is(equalTo(expectedAudioClipsFilesInfo[index])))
        }
    }

    private fun getMissingExpectedAudioClipsErrorMessage(expectedAudioClips: List<AudioClipFileInfo>): String {
        val differencesList = expectedAudioClips - actualAudioClipsFilesInfo
        return "There were ${differencesList.size} audio clips files that were not generated. \n" +
            "clips locations in seconds: ${differencesList.map { it.audioClipFileName }}"
    }
}

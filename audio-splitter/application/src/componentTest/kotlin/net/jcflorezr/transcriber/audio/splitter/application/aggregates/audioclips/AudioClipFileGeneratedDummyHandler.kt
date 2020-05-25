package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audioclips

import io.vertx.junit5.VertxTestContext
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.core.config.util.TestUtils.waitAtMostTenSecondsByOneSecondIntervals
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Event
import net.jcflorezr.transcriber.core.domain.EventHandler
import net.jcflorezr.transcriber.core.domain.EventRouter
import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo
import net.jcflorezr.transcriber.core.domain.events.audioclips.AudioClipFileGenerated
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.jupiter.api.Assertions.assertTrue

@ObsoleteCoroutinesApi
class AudioClipFileGeneratedDummyHandler(
    private val tempLocalDirectory: String
) : EventHandler<Event<AggregateRoot>> {

    private val audioClipFileActor = AudioClipFileActor()
    private val audioClipsFilesGenerated = mutableMapOf<String, MutableList<AudioClipFileInfo>>()

    init {
        EventRouter.register(AudioClipFileGenerated::class.java, this)
    }

    override suspend fun execute(event: Event<AggregateRoot>) {
        val audioClipFileGenerated = event as AudioClipFileGenerated
        audioClipFileActor.mainActor.send(StoreAudioClipFileReceived(audioClipFileGenerated.audioClipFileInfo))
    }

    suspend fun assertAudioClipsFiles(audioFileName: String, testContext: VertxTestContext) = withContext(Dispatchers.IO) {
        waitAtMostTenSecondsByOneSecondIntervals().until { audioClipsFilesGenerated.containsKey(audioFileName) }
        val actualAudioClipsFilesList = audioClipsFilesGenerated[audioFileName]
            ?: throw IllegalStateException("Probably actual audio clips files were deleted during the assertion process")
        val expectedAudioClipsFiles = File("$tempLocalDirectory/$audioFileName").listFiles()
            ?: throw FileNotFoundException("Directory containing the generated audio clips files was not found: $tempLocalDirectory")

        assertThat(actualAudioClipsFilesList.size, Is(equalTo(expectedAudioClipsFiles.size)))
            actualAudioClipsFilesList
                .sortedWith(compareBy({ it.hours }, { it.minutes }, { it.seconds }, { it.tenthsOfSecond }))
                .forEach { actualAudioClip ->
                    val actualAudioClipFile = actualAudioClip.run {
                        File("$tempLocalDirectory/$audioFileName/$audioClipFileName.$audioClipFileExtension") }
                    val expectedAudioClipFile = expectedAudioClipsFiles.find { it.name == actualAudioClipFile.name }
                        ?: throw FileNotFoundException("Expected audio clip file was not found: ${actualAudioClipFile.name}")
                    assertTrue(
                        IOUtils.contentEquals(FileInputStream(actualAudioClipFile), FileInputStream(expectedAudioClipFile)))
                }
        testContext.completeNow()
    }

    private inner class AudioClipFileActor {

        val mainActor: SendChannel<AudioClipFileReceivedMsg> =
            CoroutineScope(Dispatchers.Default).activeSegmentsActor()

        private fun CoroutineScope.activeSegmentsActor() = actor<AudioClipFileReceivedMsg> {
            for (msg in channel) {
                when (msg) {
                    is StoreAudioClipFileReceived -> {
                        val audioFileName = msg.audioClipFileInfo.sourceAudioFileName
                        audioClipsFilesGenerated.getOrPut(key = audioFileName) { mutableListOf() }
                            .also { audioClipsInfoList -> audioClipsInfoList.add(msg.audioClipFileInfo) }
                    }
                }
            }
        }
    }
}

/*
    Family classes for actors which process the Audio Clips Files received through events
 */

sealed class AudioClipFileReceivedMsg
data class StoreAudioClipFileReceived(val audioClipFileInfo: AudioClipFileInfo) : AudioClipFileReceivedMsg()

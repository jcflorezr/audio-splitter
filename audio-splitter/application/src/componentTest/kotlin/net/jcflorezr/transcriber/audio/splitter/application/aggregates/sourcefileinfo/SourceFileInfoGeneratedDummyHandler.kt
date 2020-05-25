package net.jcflorezr.transcriber.audio.splitter.application.aggregates.sourcefileinfo

import io.vertx.junit5.VertxTestContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import net.jcflorezr.transcriber.audio.splitter.adapters.repositories.sourcefileinfo.DefaultSourceFileInfoRepository
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.events.sourcefileinfo.AudioSourceFileInfoGenerated
import net.jcflorezr.transcriber.core.config.util.TestUtils.waitAtMostTenSecondsByOneSecondIntervals
import net.jcflorezr.transcriber.core.domain.AggregateRoot
import net.jcflorezr.transcriber.core.domain.Event
import net.jcflorezr.transcriber.core.domain.EventHandler
import net.jcflorezr.transcriber.core.domain.EventRouter
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat

sealed class SourceFileInfoGeneratedMsg
data class StoreSourceFileInfoGenerated(val sourceFileInfoGenerated: AudioSourceFileInfoGenerated) : SourceFileInfoGeneratedMsg()

@ObsoleteCoroutinesApi
class SourceFileInfoGeneratedDummyHandler(
    private val sourceFileInfoRepository: DefaultSourceFileInfoRepository
) : EventHandler<Event<AggregateRoot>> {

    private val sourceFileInfoActor = SourceFileInfoActor()
    private val expectedSourceFilesInfo = mutableListOf<AudioSourceFileInfo>()

    init {
        EventRouter.register(AudioSourceFileInfoGenerated::class.java, this)
    }

    override suspend fun execute(event: Event<AggregateRoot>) {
        val sourceFileInfoGenerated = event as AudioSourceFileInfoGenerated
        sourceFileInfoActor.mainActor.send(StoreSourceFileInfoGenerated(sourceFileInfoGenerated))
    }

    suspend fun assertSourceFileInfo(audioFileName: String, testContext: VertxTestContext) {
        val actualSourceFileInfo = sourceFileInfoRepository.findBy(audioFileName)
        expectedSourceFilesInfo.contains(actualSourceFileInfo)
        waitAtMostTenSecondsByOneSecondIntervals().untilAsserted {
            assertThat(expectedSourceFilesInfo, hasItem(actualSourceFileInfo))
        }
        testContext.completeNow()
    }

    private inner class SourceFileInfoActor {

        val mainActor: SendChannel<SourceFileInfoGeneratedMsg> =
            CoroutineScope(Dispatchers.Default).audioTranscriptionsActor()

        private fun CoroutineScope.audioTranscriptionsActor() = actor<SourceFileInfoGeneratedMsg> {
            for (msg in channel) {
                when (msg) {
                    is StoreSourceFileInfoGenerated ->
                        expectedSourceFilesInfo.add(msg.sourceFileInfoGenerated.audioSourceFileInfo)
                }
            }
        }
    }
}

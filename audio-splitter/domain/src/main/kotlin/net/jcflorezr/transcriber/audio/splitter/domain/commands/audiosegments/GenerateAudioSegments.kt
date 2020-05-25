package net.jcflorezr.transcriber.audio.splitter.domain.commands.audiosegments

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.AudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegment
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegments
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.audio.splitter.domain.events.audiosegments.AudioSegmentsGenerated
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.audiosegments.AudioSegmentsRepository
import net.jcflorezr.transcriber.audio.splitter.domain.ports.repositories.sourcefileinfo.SourceFileInfoRepository
import net.jcflorezr.transcriber.core.domain.Command
import net.jcflorezr.transcriber.core.domain.EventDispatcher

@ObsoleteCoroutinesApi
class GenerateAudioSegments(
    private val sourceFileInfoRepository: SourceFileInfoRepository,
    private val audioSegmentsRepository: AudioSegmentsRepository,
    private val eventDispatcher: EventDispatcher
) : Command<AudioSegment> {

    private val actors = Actors()
    private val sourceAudioFilesInfo: MutableMap<String, AudioSourceFileInfo> = mutableMapOf()
    private val sourceAudioFilesSegments: MutableMap<String, MutableList<BasicAudioSegment>> = mutableMapOf()

    override suspend fun execute(aggregateRoot: AudioSegment) {
        audioSegmentsRepository.save(audioSegment = aggregateRoot)
        actors.mainActor.send(StoreAudioSegmentReceived(aggregateRoot.toBasicAudioSegment()))
    }

    private suspend fun getNumberOfExpectedAudioSegments(audioFileName: String) =
        sourceAudioFilesInfo
            .getOrPut(key = audioFileName) { sourceFileInfoRepository.findBy(audioFileName) }
            .audioContentInfo
            .numOfAudioSegments

    private fun getSourceAudioFilesSegments(audioFileName: String) =
        sourceAudioFilesSegments.getOrPut(key = audioFileName) { mutableListOf() }

    // ==== Actors ==== //

    private inner class Actors {
        val mainActor: SendChannel<AudioSegmentReceivedMsg> = CoroutineScope(Dispatchers.Default).mainActor()
        private val audioFilesSegmentsActors: MutableMap<String, SendChannel<AudioSegmentReceivedMsg>> = mutableMapOf()

        private fun CoroutineScope.mainActor() = actor<AudioSegmentReceivedMsg> {
            for (msg in channel) {
                when (msg) {
                    is StoreAudioSegmentReceived -> {
                        val basicAudioSegment = msg.basicAudioSegment
                        val audioFileName = basicAudioSegment.sourceAudioFileName
                        getSourceAudioFilesSegments(audioFileName)
                            .also { storedAudioSegments ->
                                storedAudioSegments.add(basicAudioSegment)
                                audioFilesSegmentsActors
                                    .getOrPut(key = audioFileName) { CoroutineScope(Dispatchers.Default).audioSegmentsActor() }
                                    .send(IncrementNumOfAudioSegmentsReceived(audioFileName))
                            }
                    }
                }
            }
        }

        private fun CoroutineScope.audioSegmentsActor() = actor<AudioSegmentReceivedMsg> {
            var counter = 0
            for (msg in channel) {
                when (msg) {
                    is IncrementNumOfAudioSegmentsReceived ->
                        if (++counter == getNumberOfExpectedAudioSegments(msg.audioFileName)) {
                            val audioSegments = getSourceAudioFilesSegments(msg.audioFileName)
                            eventDispatcher.publish(AudioSegmentsGenerated(BasicAudioSegments(audioSegments)))
                            audioFilesSegmentsActors.remove(msg.audioFileName)
                        }
                }
            }
        }
    }
}

/*
    Family classes for actors which process the number of Audio Segments received per Audio File
 */
sealed class AudioSegmentReceivedMsg
data class IncrementNumOfAudioSegmentsReceived(val audioFileName: String) : AudioSegmentReceivedMsg()
data class StoreAudioSegmentReceived(val basicAudioSegment: BasicAudioSegment) : AudioSegmentReceivedMsg()

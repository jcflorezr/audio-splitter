package net.jcflorezr.transcriber.audio.splitter.application.aggregates.audiosegments

import java.io.File
import javax.sound.sampled.AudioSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application.AudioFrameProcessor
import net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audiosegments.application.AudioSegmentsService

class AudioSegmentsServiceImpl(
    private val audioFrameProcessor: AudioFrameProcessor
) : AudioSegmentsService {

    override suspend fun generateAudioSegments(audioContentInfo: AudioContentInfo, audioFile: File) =
        withContext(Dispatchers.IO) {
            AudioSystem.getAudioInputStream(audioFile)
                .use { stream -> audioFrameProcessor.processAudioFrame(audioContentInfo, stream, audioFile) }
        }
}

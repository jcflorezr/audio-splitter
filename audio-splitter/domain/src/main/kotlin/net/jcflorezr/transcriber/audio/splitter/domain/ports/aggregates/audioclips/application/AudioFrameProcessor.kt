package net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.audioclips.application

import java.io.File
import javax.sound.sampled.AudioInputStream
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioContentInfo

interface AudioFrameProcessor {

    suspend fun processAudioFrame(audioContentInfo: AudioContentInfo, stream: AudioInputStream, audioFile: File)
}

package net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileMetadata
import java.io.File

interface AudioFileMetadataGenerator {
    fun retrieveAudioFileMetadata(audioFile: File): AudioSourceFileMetadata
}
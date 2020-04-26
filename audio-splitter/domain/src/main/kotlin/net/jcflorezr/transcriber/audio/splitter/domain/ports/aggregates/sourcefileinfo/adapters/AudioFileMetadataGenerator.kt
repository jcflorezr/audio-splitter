package net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.adapters

import java.io.File
import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileMetadata

interface AudioFileMetadataGenerator {
    fun retrieveAudioFileMetadata(audioFile: File): AudioSourceFileMetadata
}

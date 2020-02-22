package net.jcflorezr.audio.splitter.domain.sourcefile

import java.io.File

interface AudioFileMetadataGenerator {
    fun retrieveAudioFileMetadata(audioFile: File): AudioSourceFileMetadata
}
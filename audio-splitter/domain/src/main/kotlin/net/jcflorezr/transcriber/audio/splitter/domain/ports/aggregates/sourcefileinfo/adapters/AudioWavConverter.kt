package net.jcflorezr.transcriber.audio.splitter.domain.ports.aggregates.sourcefileinfo.adapters

import java.io.File

interface AudioWavConverter {
    fun createAudioWavFile(originalAudioFile: File): File?
}

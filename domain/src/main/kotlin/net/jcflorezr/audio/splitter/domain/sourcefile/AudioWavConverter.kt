package net.jcflorezr.audio.splitter.domain.sourcefile

import java.io.File

interface AudioWavConverter {
    fun createAudioWavFile(originalAudioFile: File): File?
}
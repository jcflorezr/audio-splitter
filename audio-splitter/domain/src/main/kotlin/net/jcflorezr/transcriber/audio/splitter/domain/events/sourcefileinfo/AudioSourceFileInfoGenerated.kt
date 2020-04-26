package net.jcflorezr.transcriber.audio.splitter.domain.events.sourcefileinfo

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.sourcefileinfo.AudioSourceFileInfo
import net.jcflorezr.transcriber.core.domain.Event

class AudioSourceFileInfoGenerated(val audioSourceFileInfo: AudioSourceFileInfo) : Event<AudioSourceFileInfo>(audioSourceFileInfo)

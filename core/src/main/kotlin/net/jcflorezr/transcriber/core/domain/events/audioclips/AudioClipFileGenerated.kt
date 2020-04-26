package net.jcflorezr.transcriber.core.domain.events.audioclips

import net.jcflorezr.transcriber.core.domain.Event
import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo

class AudioClipFileGenerated(val audioClipFileInfo: AudioClipFileInfo) : Event<AudioClipFileInfo>(audioClipFileInfo)

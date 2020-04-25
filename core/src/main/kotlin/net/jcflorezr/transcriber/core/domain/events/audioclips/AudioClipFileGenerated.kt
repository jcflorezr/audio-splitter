package net.jcflorezr.transcriber.core.domain.events.audioclips

import net.jcflorezr.transcriber.core.domain.aggregates.audioclips.AudioClipFileInfo
import net.jcflorezr.transcriber.core.domain.Event

class AudioClipFileGenerated (val audioClipFileInfo: AudioClipFileInfo) : Event<AudioClipFileInfo>(audioClipFileInfo)
package net.jcflorezr.transcriber.audio.splitter.domain.events.audioclips

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audioclips.AudioClip
import net.jcflorezr.transcriber.core.domain.Event

class AudioClipInfoGenerated(val audioClip: AudioClip) : Event<AudioClip>(audioClip)
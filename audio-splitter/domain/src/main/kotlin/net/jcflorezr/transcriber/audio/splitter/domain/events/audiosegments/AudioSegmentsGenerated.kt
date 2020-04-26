package net.jcflorezr.transcriber.audio.splitter.domain.events.audiosegments

import net.jcflorezr.transcriber.audio.splitter.domain.aggregates.audiosegments.BasicAudioSegments
import net.jcflorezr.transcriber.core.domain.Event

class AudioSegmentsGenerated(val audioSegments: BasicAudioSegments) : Event<BasicAudioSegments>(audioSegments)

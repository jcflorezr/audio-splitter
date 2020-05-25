package net.jcflorezr.transcriber.audio.transcriber.domain.events.audiotranscriptions

import net.jcflorezr.transcriber.audio.transcriber.domain.aggregates.audiotranscriptions.AudioTranscription
import net.jcflorezr.transcriber.core.domain.Event

class AudioTranscriptionGenerated(val audioTranscription: AudioTranscription) : Event<AudioTranscription>(audioTranscription)

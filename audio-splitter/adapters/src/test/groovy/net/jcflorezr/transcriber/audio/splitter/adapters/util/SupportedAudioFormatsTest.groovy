package net.jcflorezr.transcriber.audio.splitter.adapters.util

import net.jcflorezr.transcriber.core.exception.AudioSourceException
import net.jcflorezr.transcriber.core.util.SupportedAudioFormats
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

class SupportedAudioFormatsTest extends Specification {

    @Unroll
    def "#mimeType"() {

        when:
        def actualMimeType = new SupportedAudioFormats.Companion().findFileType(mimeType).getMimeType()

        then:
        actualMimeType == expectedMimeType

        where:
        mimeType         | expectedMimeType
        "audio/x-wav"    | SupportedAudioFormats.WAV.mimeType
        "audio/vnd.wave" | SupportedAudioFormats.WAVE.mimeType
        "audio/x-flac"   | SupportedAudioFormats.FLAC.mimeType
        "audio/mpeg"     | SupportedAudioFormats.MP3.mimeType
        "audio/x-mpeg-3" | SupportedAudioFormats.MP3_1.mimeType
    }

    @Unroll
    def "unsupported mime types"() {
        given:
        def supportedMimeTypes = Arrays.asList(SupportedAudioFormats.values())
            .stream().map({format -> format.mimeType }).collect(Collectors.toList())

        when:
        new SupportedAudioFormats.Companion().findFileType(mimeType).getMimeType()

        then:
        def actualException = thrown(AudioSourceException)
        def expectedException =
            new AudioSourceException.Companion().audioFormatTypeNotSupported(mimeType, supportedMimeTypes)
        verifyAll {
            assert actualException.errorCode == expectedException.errorCode
            assert actualException.message == expectedException.message
            assert actualException.suggestion == expectedException.suggestion
        }

        where:
        mimeType           | _
        "application/json" | _
        "text/plain"       | _
        "text/html"        | _
        "invalid"          | _
        ""                 | _
    }

    @Unroll
    def "#fileExtension"() {

        when:
        def actualFileExtension = new SupportedAudioFormats.Companion().findExtension(fileExtension).getExtension()

        then:
        actualFileExtension == expectedFileExtension

        where:
        fileExtension | expectedFileExtension
        "wav"         | SupportedAudioFormats.WAV.extension
        "wav"         | SupportedAudioFormats.WAVE.extension
        "flac"        | SupportedAudioFormats.FLAC.extension
        "mp3"         | SupportedAudioFormats.MP3.extension
        "mp3"         | SupportedAudioFormats.MP3_1.extension
    }

    @Unroll
    def "unsupported files extensions"() {
        given:
        def supportedMimeTypes = Arrays.asList(SupportedAudioFormats.values())
            .stream().map({format -> format.extension })
            .distinct()
            .collect(Collectors.toList())

        when:
        new SupportedAudioFormats.Companion().findExtension(fileExtension).getExtension()

        then:
        def actualException = thrown(AudioSourceException)
        def expectedException =
            new AudioSourceException.Companion().audioFileExtensionNotSupported(fileExtension, supportedMimeTypes)
        verifyAll {
            assert actualException.errorCode == expectedException.errorCode
            assert actualException.message == expectedException.message
            assert actualException.suggestion == expectedException.suggestion
        }

        where:
        fileExtension | _
        "json"        | _
        "txt"         | _
        "html"        | _
        "invalid"     | _
        ""            | _
    }
}

package net.jcflorezr.audio.splitter.adapters.sourcefile

import net.jcflorezr.audio.splitter.adapters.util.SupportedAudioFormats
import org.apache.tika.Tika
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class JavaAudioWavConverterTest extends Specification {

    private static Class<? extends JavaAudioWavConverterTest> thisClass = this.class
    private static tempLocalDirectoryPath = thisClass.getResource("/temp-converted-files/").getPath()
    private JavaAudioWavConverter javaAudioWavConverter = new JavaAudioWavConverter(tempLocalDirectoryPath)

    def @Shared wavAudioFileFromMp3 = "Create WAV audio file from MP3 audio file"
    def @Shared wavAudioFileFromFlac = "Create WAV audio file from FLAC audio file"
    def @Shared wavAudioFileFromWav = "Create WAV audio file from WAV audio file"

    @Unroll
    def "#testTitle"() {

        when:
        def actualWavAudioFile = javaAudioWavConverter.createAudioWavFile(new File(audioFilePath))

        then:
        def actualAudioContentType = getAudioContentType(actualWavAudioFile)
        actualAudioContentType == expectedAudioContentType

        cleanup:
        if (actualWavAudioFile) {
            actualWavAudioFile.delete()
        }

        where:
        audioFormat                | audioFilePath                 | expectedAudioContentType                 | testTitle
        SupportedAudioFormats.MP3  | getAudioFilePath(audioFormat) | SupportedAudioFormats.WAVE.getMimeType() | wavAudioFileFromMp3
        SupportedAudioFormats.FLAC | getAudioFilePath(audioFormat) | SupportedAudioFormats.WAVE.getMimeType() | wavAudioFileFromFlac
        SupportedAudioFormats.WAV  | getAudioFilePath(audioFormat) | null                                     | wavAudioFileFromWav

    }

    private static String getAudioFilePath(SupportedAudioFormats audioFormat) {
        thisClass.getResource("/source-file/test-audio-mono.$audioFormat.extension").getPath()
    }

    private static String getAudioContentType(File wavFile) {
        if (wavFile) {
            return new Tika().detect(wavFile)
        } else {
            null
        }
    }
}

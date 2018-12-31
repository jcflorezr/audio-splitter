package biz.source_code.dsp.sound

import javafx.application.Application.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.TestRootConfig
import net.jcflorezr.model.AudioFileMetadata
import net.jcflorezr.model.InitialConfiguration
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File

/**
 * These tests have been marked as Integration Tests because they take
 * too long to validate the big amount of audio signals and they also
 * re-construct an audio file from several parts of audio signals
 */
// TODO: create net.jcflorezr package in test resource folder and put all the existing folders in there
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestRootConfig::class])
class AudioIoImplIntegrationTest {

    @Autowired
    private lateinit var audioIo: AudioIo

//    companion object {
//        private val MAPPER = ObjectMapper().registerKotlinModule()
//    }

    private val testResourcesPath: String
    private val thisClass: Class<AudioIoImplIntegrationTest> = this.javaClass

    init {
        testResourcesPath = thisClass.getResource("/sound/").path
    }

    @Test
    fun retrieveSignalFromFileWithBackgroundNoiseAndLowVoiceVolume() {
        val audioFileLocation =
            testResourcesPath + "background-noise-low-volume/background-noise-low-volume.wav"
        retrieveSignalFromAudioFile(audioFileLocation, File(audioFileLocation).name)
    }

    @Test
    fun retrieveSignalFromFileWithApplause() {
        val audioFileLocation = testResourcesPath + "with-applause/with-applause.wav"
        retrieveSignalFromAudioFile(audioFileLocation, File(audioFileLocation).name)
    }

    @Test
    fun retrieveSignalFromFileWithStrongBackgroundNoise() {
        val audioFileLocation = testResourcesPath + "strong-background-noise/strong-background-noise.wav"
        retrieveSignalFromAudioFile(audioFileLocation, File(audioFileLocation).name)
    }

    @Test
    fun saveAudioFile() {

//        File(testResourcesPath).listFiles().map {
//                signalJsonFile ->
//            audioIo.saveAudioFile(testResourcesPath + signalJsonFile.name, ".wav", MAPPER.readValue<AudioSignalKt>(signalJsonFile))
//        }
        //audioIo.saveAudioFile(testResourcesPath + "any-name", ".wav", MAPPER.readValue(File(testResourcesPath + "signalcita.json"), AudioSignalKt::class.java))


//        val audioFileName = testResourcesPath + "test-audio-mono-22050.wav"
//
//        val audioIo = AudioIo2()
//        val audioSignal = audioIo.retrieveAudioSignalFromWavFile(audioFileName)
//
//        audioIo.saveAudioFile(audioFileName, ".wav", audioSignal)
//
//        val expectedAudioClipSignal = MAPPER.readValue(thisClass!!.getResourceAsStream("/audiocontent/mysignal2.json"), AudioSignal::class.java)
//
//        assertThat(audioSignal, `is`(expectedAudioClipSignal))

    }

    private fun retrieveSignalFromAudioFile(audioFileLocation: String, audioFileName: String) {
        audioIo.generateAudioSignalFromAudioFile(
            InitialConfiguration(
                audioFileLocation = audioFileLocation,
                convertedAudioFileLocation = audioFileLocation,
                audioFileMetadata = AudioFileMetadata(audioFileName = audioFileName)
            )
        )
    }

}
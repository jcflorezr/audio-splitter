package net.jcflorezr.signal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.TestRootConfig
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.model.AudioFileMetadata
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.util.AudioFormats
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import java.io.FileInputStream

// TODO: create net.jcflorezr package in test resource folder and put all the existing folders in there
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestRootConfig::class])
class AudioIoImplIntegrationTest {

    @Autowired
    private lateinit var audioIo: AudioIo

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val signalResourcesPath: String
    private val clipResourcesPath: String
    private val thisClass: Class<AudioIoImplIntegrationTest> = this.javaClass

    init {
        signalResourcesPath = thisClass.getResource("/signal/").path
        clipResourcesPath = thisClass.getResource("/clip/").path
    }

    @Test
    fun retrieveSignalFromFileWithBackgroundNoiseAndLowVoiceVolume() {
        val audioFileLocation = signalResourcesPath + "background-noise-low-volume/background-noise-low-volume.wav"
        retrieveSignalFromAudioFile(audioFileLocation, File(audioFileLocation).name)
    }

    @Test
    fun retrieveSignalFromFileWithApplause() {
        val audioFileLocation = signalResourcesPath + "with-applause/with-applause.wav"
        retrieveSignalFromAudioFile(audioFileLocation, File(audioFileLocation).name)
    }

    @Test
    fun retrieveSignalFromFileWithStrongBackgroundNoise() {
        val audioFileLocation = signalResourcesPath + "strong-background-noise/strong-background-noise.wav"
        retrieveSignalFromAudioFile(audioFileLocation, File(audioFileLocation).name)
    }

    @Test
    fun createAudioClipsForAudioFileWithBackgroundNoiseAndLowVoiceVolume() {
        createAudioClips("background-noise-low-volume")
    }

    @Test
    fun createAudioClipsForAudioFileWithApplause() {
        createAudioClips("with-applause")
    }

    @Test
    fun createAudioClipsForAudioFileWithStrongBackgroundNoise() {
        createAudioClips("strong-background-noise")
    }

    private fun retrieveSignalFromAudioFile(audioFileLocation: String, audioFileName: String) = runBlocking {
        audioIo.generateAudioSignalFromAudioFile(
            InitialConfiguration(
                audioFileLocation = audioFileLocation,
                convertedAudioFileLocation = audioFileLocation,
                audioFileMetadata = AudioFileMetadata(audioFileName = audioFileName)
            )
        )
    }

    private fun createAudioClips(folderName: String) = runBlocking {
        val audioClipsSignalFolder = "$clipResourcesPath/$folderName/signal"
        val audioClipsFilesFolder = "$clipResourcesPath/$folderName/audio-file"
        val testAudioClipsFilesFolder = "$audioClipsFilesFolder/test"
        File(audioClipsSignalFolder).listFiles()
        .forEach { clipSignalFile ->
            val baseFileName = clipSignalFile.nameWithoutExtension
            File(testAudioClipsFilesFolder).takeIf { !it.exists() }?.mkdirs()
            val expectedAudioClipSignal = MAPPER.readValue(clipSignalFile, AudioClipSignal::class.java)
            audioIo.saveAudioFile(
                fileName = "$testAudioClipsFilesFolder/$baseFileName",
                extension = AudioFormats.FLAC.extension,
                signal = expectedAudioClipSignal.signal,
                sampleRate = expectedAudioClipSignal.sampleRate
            )
            val actualAudioClipFile = File("$testAudioClipsFilesFolder/$baseFileName${AudioFormats.FLAC.extension}")
            val expectedAudioClipFile = File("$audioClipsFilesFolder/$baseFileName${AudioFormats.FLAC.extension}")
            assertThat(actualAudioClipFile.name, Is(equalTo(expectedAudioClipFile.name)))
            assertTrue(IOUtils.contentEquals(FileInputStream(actualAudioClipFile), FileInputStream(expectedAudioClipFile)))
            actualAudioClipFile.delete()
        }
    }

}
package net.jcflorezr.signal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.TestRootConfig
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.model.AudioFileMetadata
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.util.AudioFormats
import net.jcflorezr.util.PropsUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.hamcrest.CoreMatchers.`is` as Is
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File
import java.io.FileInputStream

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestRootConfig::class])
class AudioIoImplIntegrationTest {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var audioIo: AudioIo

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val tempFilesFolder: String
    private val signalResourcesPath: String
    private val clipResourcesPath: String
    private val thisClass: Class<AudioIoImplIntegrationTest> = this.javaClass

    init {
        tempFilesFolder = thisClass.getResource("/temp-converted-files").path
        signalResourcesPath = thisClass.getResource("/signal").path
        clipResourcesPath = thisClass.getResource("/clip").path
    }

    @Test
    fun retrieveSignalFromFileWithBackgroundNoiseAndLowVoiceVolume() {
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume"))
        val audioFileLocation = "$signalResourcesPath/background-noise-low-volume/background-noise-low-volume.wav"
        retrieveSignalFromAudioFile(audioFileLocation)
    }

    @Test
    fun retrieveSignalFromFileWithApplause() {
        propsUtils.setTransactionId(sourceAudioFile = File("with-applause"))
        val audioFileLocation = "$signalResourcesPath/with-applause/with-applause.wav"
        retrieveSignalFromAudioFile(audioFileLocation)
    }

    @Test
    fun retrieveSignalFromFileWithStrongBackgroundNoise() {
        propsUtils.setTransactionId(sourceAudioFile = File("strong-background-noise"))
        val audioFileLocation = "$signalResourcesPath/strong-background-noise/strong-background-noise.wav"
        retrieveSignalFromAudioFile(audioFileLocation)
    }

    private fun retrieveSignalFromAudioFile(audioFileLocation: String) = runBlocking {
        val audioFile = File(audioFileLocation)
        val tempFile = File("$tempFilesFolder/${audioFile.name}")
        FileUtils.copyFile(audioFile, tempFile)
        audioIo.generateAudioSignalFromAudioFile(
            InitialConfiguration(
                audioFileName = tempFile.absolutePath,
                audioFileMetadata = AudioFileMetadata(audioFileName = audioFile.name)
            )
        )
        tempFile.delete()
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
                sampleRate = expectedAudioClipSignal.sampleRate,
                transactionId = "any-transaction-id"
            )
            val actualAudioClipFile = File("$testAudioClipsFilesFolder/$baseFileName${AudioFormats.FLAC.extension}")
            val expectedAudioClipFile = File("$audioClipsFilesFolder/$baseFileName${AudioFormats.FLAC.extension}")
            assertThat(actualAudioClipFile.name, Is(equalTo(expectedAudioClipFile.name)))
            assertTrue(IOUtils.contentEquals(FileInputStream(actualAudioClipFile), FileInputStream(expectedAudioClipFile)))
            actualAudioClipFile.delete()
        }
    }
}
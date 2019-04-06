package net.jcflorezr.entrypoint

import net.jcflorezr.config.TestRootConfig
import net.jcflorezr.exception.SourceAudioFileValidationException
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.storage.BucketClient
import org.apache.commons.io.FilenameUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when` as When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestRootConfig::class])
class AudioSplitterImplTest {

    @Autowired
    private lateinit var bucketClient: BucketClient

    private val testResourcesPath: String
    private val tempConvertedFilesPath: String
    private val thisClass: Class<AudioSplitterImplTest> = this.javaClass

    init {
        testResourcesPath = thisClass.getResource("/entrypoint").path
        tempConvertedFilesPath = thisClass.getResource("/temp-converted-files").path
    }

    @Autowired
    private lateinit var audioSplitter: AudioSplitter

    @Test
    fun generateAudioClipsFromMp3File() {
        generateAudioClipsFromFile(audioFileName = "test-audio-mono.mp3")
    }

    @Test
    fun generateAudioClipsFromFlacFile() {
        generateAudioClipsFromFile(audioFileName = "test-audio-mono.flac")
    }

    private fun generateAudioClipsFromFile(audioFileName: String) {
        val audioFileLocation = testResourcesPath
        val convertedAudioFileName = FilenameUtils.getBaseName(audioFileName) + ".wav"
        When(bucketClient.downloadSourceFileFromBucket("$audioFileLocation/$audioFileName"))
                .thenReturn(File("$audioFileLocation/$audioFileName"))
        try {
            audioSplitter.splitAudioIntoClips(
                configuration = InitialConfiguration(audioFileName = "$audioFileLocation/$audioFileName")
            )
        } finally {
            File("$tempConvertedFilesPath/$convertedAudioFileName").delete()
        }
    }

    @Test(expected = SourceAudioFileValidationException::class)
    fun localConfiguration_shouldThrowMandatoryFieldsMissingException() {
        audioSplitter.splitAudioIntoClips(
            configuration = InitialConfiguration(
                audioFileName = ""
            )
        )
    }

    @Test(expected = SourceAudioFileValidationException::class)
    fun localConfiguration_shouldThrowAudioFileDoesNotExistException() {
        val audioFileLocation = "any-location"
        When(bucketClient.downloadSourceFileFromBucket(audioFileLocation)).thenReturn(File(audioFileLocation))
        audioSplitter.splitAudioIntoClips(
            configuration = InitialConfiguration(audioFileName = "any-location")
        )
    }

    @Test(expected = SourceAudioFileValidationException::class)
    fun localConfiguration_shouldThrowAudioFileIsDirectoryException() {
        val audioFileLocation = testResourcesPath
        When(bucketClient.downloadSourceFileFromBucket("$audioFileLocation/"))
            .thenReturn(File("$audioFileLocation/"))
        audioSplitter.splitAudioIntoClips(
            configuration = InitialConfiguration(audioFileName = "$audioFileLocation/")
        )
    }
}
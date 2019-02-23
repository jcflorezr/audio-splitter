package net.jcflorezr.entrypoint

import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.TestRootConfig
import net.jcflorezr.model.InitialConfiguration
import org.apache.commons.io.FilenameUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestRootConfig::class])
class AudioSplitterImplTest {

    private val testResourcesPath: String
    private val tempConvertedFilesPath: String
    private val thisClass: Class<AudioSplitterImplTest> = this.javaClass

    init {
        testResourcesPath = thisClass.getResource("/entrypoint/").path
        tempConvertedFilesPath = thisClass.getResource("/temp-converted-files").path
    }

    @Autowired
    private lateinit var audioSplitter: AudioSplitter

    @Test
    fun generateAudioClips() = runBlocking<Unit> {
        val audioFileLocation = testResourcesPath
        val audioFileName = "test-audio-mono-22050.mp3"
        audioSplitter.splitAudioIntoClips(
            configuration = InitialConfiguration(
                audioFileLocation = audioFileLocation + audioFileName
            )
        )
        val convertedAudioFileName = FilenameUtils.getBaseName(audioFileName) + ".wav"
        File("$tempConvertedFilesPath/$convertedAudioFileName").delete()
    }

    // TODO: cover the failures asserting the Error Objects

}
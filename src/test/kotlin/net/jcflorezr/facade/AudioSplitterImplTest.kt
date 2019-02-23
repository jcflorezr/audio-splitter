package net.jcflorezr.facade

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

    private lateinit var testResourcesPath: String
    private val thisClass: Class<AudioSplitterImplTest> = this.javaClass

    @Autowired
    private lateinit var audioSplitter: AudioSplitter

    @Before
    fun setUp() {
        testResourcesPath = thisClass.getResource("/core/").path
    }

    @Test
    fun generateAudioClips() = runBlocking<Unit> {
        val audioFileLocation = testResourcesPath
        val audioFileName = "test-audio-mono-22050.mp3"
        /* TODO: this test is going to fail due to the fixed audioFileLocation
            stored in InitialConfiguration
         */
        audioSplitter.splitAudioIntoClips(
            configuration = InitialConfiguration(
                audioFileLocation = audioFileLocation + audioFileName
            )
        )
        val convertedAudioFileName = FilenameUtils.getBaseName(audioFileName) + ".wav"
        File(audioFileLocation + convertedAudioFileName).delete()
    }
}
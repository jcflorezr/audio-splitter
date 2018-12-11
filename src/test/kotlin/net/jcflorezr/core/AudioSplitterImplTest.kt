package net.jcflorezr.core

import net.jcflorezr.config.TestRootConfig
import net.jcflorezr.model.InitialConfiguration
import org.apache.commons.io.FilenameUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File

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
    fun generateAudioClips() {
        val audioFileLocation = testResourcesPath
        val audioFileName = "test-audio-mono-22050.mp3"

        /* TODO: this call spawns new threads in the background
            and assertion errors are being "swallowed"
            we should learn to implement coroutines
         */
        /* TODO: this test is going to fail due to the fixed audioFileLocation
            stored in InitialConfiguration
         */
        audioSplitter.generateAudioClips(
            configuration = InitialConfiguration(
                audioFileLocation = audioFileLocation + audioFileName
            )
        )
        Thread.sleep(2000L)
        val convertedAudioFileName = FilenameUtils.getBaseName(audioFileName) + ".wav"
        File(audioFileLocation + convertedAudioFileName).delete()
    }
}
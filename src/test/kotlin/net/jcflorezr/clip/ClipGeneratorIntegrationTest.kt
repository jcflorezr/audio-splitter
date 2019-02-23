package net.jcflorezr.clip

import biz.source_code.dsp.model.AudioSignalKt
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.config.TestClipsGeneratorConfig
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioFormatEncodings
import net.jcflorezr.model.AudioSourceInfo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestClipsGeneratorConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClipGeneratorTest {

    @Autowired
    private lateinit var clipGenerator: ClipGenerator
    @Autowired
    private lateinit var audioSignalDao: AudioSignalDao

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val signalResourcesPath: String
    private val clipsResourcesPath: String
    private val thisClass: Class<ClipGeneratorTest> = this.javaClass

    init {
        signalResourcesPath = thisClass.getResource("/sound/").path
        clipsResourcesPath = thisClass.getResource("/clip/").path
    }

    @Test
    fun generateAudioClipsForFileWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        generateAudioClips(
            path = clipsResourcesPath + "background-noise-low-volume/background-noise-low-volume.json"
        )
    }

    @Test
    fun generateAudioClipsInfoForFileWithApplause() = runBlocking {
        generateAudioClips(
            path = clipsResourcesPath + "with-applause/with-applause.json"
        )
    }

    @Test
    fun generateAudioClipsInfoForFileWithStrongBackgroundNoise() = runBlocking {
        generateAudioClips(
            path = clipsResourcesPath + "strong-background-noise/strong-background-noise.json"
        )
    }

    private fun generateAudioClips(path: String) {
        Mockito.`when`(audioSignalDao.retrieveAudioSignalsFromRange(anyString(), anyDouble(), anyDouble()))
                .thenReturn(arrayListOf(AudioSignalKt(
                        audioFileName = "a",
                        index = 1,
                        sampleRate = 0,
                        totalFrames = 0,
                        initialPosition = 0,
                        initialPositionInSeconds = 0.0f,
                        endPosition = 1,
                        endPositionInSeconds = 1.1f,
                        data = arrayOf(floatArrayOf()),
                        dataInBytes = byteArrayOf(),
                        audioSourceInfo = AudioSourceInfo(
                                channels = 0,
                                sampleRate = 0,
                                sampleSizeBits = 0,
                                frameSize = 0,
                                sampleSize = 0,
                                bigEndian = true,
                                encoding = AudioFormatEncodings.PCM_SIGNED
                        )
                )))
        val clipInfoListType = MAPPER.typeFactory.constructCollectionType(List::class.java, AudioClipInfo::class.java)
        val audioClipInfoList: List<AudioClipInfo> = MAPPER.readValue(File(path), clipInfoListType)
        clipGenerator.generateClips(audioClipInfoIterator = audioClipInfoList.listIterator())
    }

}
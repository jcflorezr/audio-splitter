package net.jcflorezr.signal

import biz.source_code.dsp.model.AudioSignalKt
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.config.TestRootConfig
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
class RmsCalculatorImplTest {

    @Autowired
    private lateinit var rmsCalculator: RmsCalculator

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val testResourcesPath: String
    private val thisClass: Class<RmsCalculatorImplTest> = this.javaClass

    init {
        testResourcesPath = thisClass.getResource("/sound/").path
    }

    @Test
    fun generateRmsInfoForFileWithBackgroundNoiseAndLowVoiceVolume() {
        generateRmsInfo(path = testResourcesPath + "background-noise-low-volume/")
    }

    @Test
    fun generateRmsInfoForFileWithApplause() {
        generateRmsInfo(path = testResourcesPath + "with-applause/")
    }

    @Test
    fun generateRmsInfoForFileWithStrongBackgroundNoise() {
        generateRmsInfo(path = testResourcesPath + "strong-background-noise/")
    }

    private fun generateRmsInfo(path: String) {
        File(path).listFiles().asSequence()
            .filter { it.extension == "json" }
            .map { signalJsonFile -> MAPPER.readValue<AudioSignalKt>(signalJsonFile) }
            .forEach { rmsCalculator.generateRmsInfo(audioSignal = it) }
        Thread.sleep(2000L)
    }

}
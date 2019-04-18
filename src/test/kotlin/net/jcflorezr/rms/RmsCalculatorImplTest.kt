package net.jcflorezr.rms

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import net.jcflorezr.broker.SignalRmsSubscriberMock
import net.jcflorezr.config.TestRootConfig
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.util.PropsUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import java.io.File

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [TestRootConfig::class])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RmsCalculatorImplTest {

    @Autowired
    private lateinit var propsUtils: PropsUtils
    @Autowired
    private lateinit var applicationCtx: ApplicationContext
    @Autowired
    private lateinit var rmsCalculator: RmsCalculator

    companion object {
        private val MAPPER = ObjectMapper().registerKotlinModule()
    }

    private val testResourcesPath: String
    private val thisClass: Class<RmsCalculatorImplTest> = this.javaClass

    init {
        testResourcesPath = thisClass.getResource("/signal/").path
    }

    @Test
    fun generateRmsInfoForFileWithBackgroundNoiseAndLowVoiceVolume() = runBlocking {
        propsUtils.setTransactionId(sourceAudioFile = File("background-noise-low-volume"))
        generateRmsInfo(path = testResourcesPath + "background-noise-low-volume/")
        val signalRmsSubscriber = applicationCtx.getBean("signalRmsSubscriberTest") as SignalRmsSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Test
    fun generateRmsInfoForFileWithApplause() = runBlocking {
        propsUtils.setTransactionId(sourceAudioFile = File("with-applause"))
        generateRmsInfo(path = testResourcesPath + "with-applause/")
        val signalRmsSubscriber = applicationCtx.getBean("signalRmsSubscriberTest") as SignalRmsSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    @Test
    fun generateRmsInfoForFileWithStrongBackgroundNoise() = runBlocking {
        propsUtils.setTransactionId(sourceAudioFile = File("strong-background-noise"))
        generateRmsInfo(path = testResourcesPath + "strong-background-noise/")
        val signalRmsSubscriber = applicationCtx.getBean("signalRmsSubscriberTest") as SignalRmsSubscriberMock
        signalRmsSubscriber.validateCompleteness()
    }

    private suspend fun generateRmsInfo(path: String) {
        File(path).listFiles().asSequence()
            .filter { it.extension == "json" }
            .map { signalJsonFile -> MAPPER.readValue<AudioSignal>(signalJsonFile) }
            .forEach { rmsCalculator.generateRmsInfo(audioSignal = it) }
    }
}
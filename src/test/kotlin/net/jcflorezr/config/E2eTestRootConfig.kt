package net.jcflorezr.config

import net.jcflorezr.broker.AudioClipInfoSubscriber
import net.jcflorezr.broker.AudioClipSignalSubscriber
import net.jcflorezr.broker.AudioSplitterProducer
import net.jcflorezr.broker.SignalRmsSubscriber
import net.jcflorezr.broker.SignalSubscriber
import net.jcflorezr.broker.SourceFileSubscriber
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.clip.ClipGenerator
import net.jcflorezr.clip.ClipGeneratorActor
import net.jcflorezr.clip.ClipGeneratorActorImpl
import net.jcflorezr.entrypoint.AudioSplitter
import net.jcflorezr.entrypoint.AudioSplitterImpl
import net.jcflorezr.exception.ExceptionHandler
import net.jcflorezr.exception.ExceptionHandlerImpl
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.rms.RmsCalculator
import net.jcflorezr.rms.RmsCalculatorImpl
import net.jcflorezr.rms.SoundZonesDetector
import net.jcflorezr.rms.SoundZonesDetectorActor
import net.jcflorezr.rms.SoundZonesDetectorActorImpl
import net.jcflorezr.signal.AudioIo
import net.jcflorezr.signal.AudioIoImpl
import net.jcflorezr.storage.BucketClient
import net.jcflorezr.util.PropsUtils
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Scope

@Configuration
@Import(value = [RedisConfig::class, CassandraConfig::class])
class E2eTestRootConfig {

    @Autowired
    private lateinit var cassandraConfig: CassandraConfig

    @Autowired
    private lateinit var redisConfig: RedisConfig

    /*
    Services
     */

    @Bean fun propsUtils(): PropsUtils = PropsUtils()

    @Bean fun bucketClient(): BucketClient = mock(BucketClient::class.java)

    @Bean fun exceptionHandler(): ExceptionHandler = ExceptionHandlerImpl(propsUtils())

    @Bean fun audioSplitter(): AudioSplitter =
        AudioSplitterImpl(propsUtils(), sourceFileTopic(), bucketClient(), exceptionHandler())

    @Bean fun audioIo(): AudioIo = AudioIoImpl(propsUtils(), signalTopic())

    @Bean fun rmsCalculator(): RmsCalculator = RmsCalculatorImpl(propsUtils(), signalRmsTopic())

    // SoundZonesDetector is a prototype bean

    @Bean fun soundZonesDetectorFactory(): () -> SoundZonesDetector = { soundZonesDetector() }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun soundZonesDetector(): SoundZonesDetector = SoundZonesDetector(propsUtils(), audioClipInfoTopic(), redisConfig.audioSignalRmsDao())

    // ClipGenerator is a prototype

    @Bean fun clipGeneratorFactory(): () -> ClipGenerator = { clipGenerator() }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun clipGenerator(): ClipGenerator =
        ClipGenerator(propsUtils(), audioClipSignalTopic(), redisConfig.audioSignalDao(), redisConfig.audioClipDao())

    /*
    Topics
     */

    @Bean fun sourceFileTopic() = Topic<InitialConfiguration>()

    @Bean fun signalTopic() = Topic<AudioSignal>()

    @Bean fun signalRmsTopic() = Topic<AudioSignalsRmsInfo>()

    @Bean fun audioClipInfoTopic() = Topic<AudioClipInfo>()

    @Bean fun audioClipSignalTopic() = Topic<AudioClipSignal>()

    /*
    Subscribers
     */

    @Bean fun sourceFileSubscriber(): Subscriber<InitialConfiguration> =
        SourceFileSubscriber(propsUtils(), exceptionHandler(), sourceFileTopic(), audioIo(), cassandraConfig.sourceFileDao())

    @Bean fun signalSubscriber(): Subscriber<AudioSignal> =
        SignalSubscriber(propsUtils(), exceptionHandler(), signalTopic(), rmsCalculator(), redisConfig.audioSignalDao())

    @Bean fun signalRmsSubscriber(): Subscriber<AudioSignalsRmsInfo> =
        SignalRmsSubscriber(propsUtils(), exceptionHandler(), signalRmsTopic(), soundZonesDetectorActor(), redisConfig.audioSignalRmsDao())

    @Bean fun audioClipInfoSubscriber(): Subscriber<AudioClipInfo> =
        AudioClipInfoSubscriber(propsUtils(), exceptionHandler(), audioClipInfoTopic(), redisConfig.audioClipDao(), clipGeneratorActor())

    @Bean fun audioClipSignalSubscriber(): Subscriber<AudioClipSignal> =
        AudioClipSignalSubscriber(propsUtils(), exceptionHandler(), bucketClient(), audioClipSignalTopic(), audioIo(), audioSplitterProducer())

    /*
    Producer
     */

    @Bean fun audioSplitterProducer(): AudioSplitterProducer = mock(AudioSplitterProducer::class.java)

    /*
    Actors
     */

    @Bean fun soundZonesDetectorActor(): SoundZonesDetectorActor =
        SoundZonesDetectorActorImpl(propsUtils(), soundZonesDetectorFactory(), redisConfig.audioSignalRmsDao())

    @Bean fun clipGeneratorActor(): ClipGeneratorActor =
        ClipGeneratorActorImpl(propsUtils(), exceptionHandler(), clipGeneratorFactory(), redisConfig.audioClipDao())
}

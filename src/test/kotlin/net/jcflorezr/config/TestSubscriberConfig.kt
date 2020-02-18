package net.jcflorezr.config

import net.jcflorezr.broker.AudioClipInfoSubscriber
import net.jcflorezr.broker.AudioClipInfoSubscriberMock
import net.jcflorezr.broker.AudioClipSignalSubscriberMock
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.clip.ClipGenerator
import net.jcflorezr.clip.ClipGeneratorActor
import net.jcflorezr.clip.ClipGeneratorActorImpl
import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioClipDaoImpl
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalDaoImpl
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.AudioSignalRmsDaoImpl
import net.jcflorezr.exception.ExceptionHandler
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.rms.SoundZonesDetector
import net.jcflorezr.rms.SoundZonesDetectorActor
import net.jcflorezr.util.PropsUtils
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope
import org.springframework.data.cassandra.core.CassandraOperations

@Configuration
@Import(value = [TestRedisConfig::class])
class SignalRmsSubscriberConfig {

    @Autowired
    private lateinit var redisConfig: TestRedisConfig

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()

    @Profile("test") @Bean fun exceptionHandler(): ExceptionHandler = mock(ExceptionHandler::class.java)

    @Profile("test") @Bean fun signalRmsTopicSubscriberTest() = Topic<AudioSignalsRmsInfo>()

    @Profile("test") @Bean fun signalRmsSubscriberSubscriberTest(): Subscriber<AudioSignalsRmsInfo> =
        mock(Subscriber::class.java) as Subscriber<AudioSignalsRmsInfo>

    @Profile("test") @Bean fun soundZonesDetectorActorSubscriberTest(): SoundZonesDetectorActor = mock(SoundZonesDetectorActor::class.java)

    // SoundZonesDetector is a prototype bean

    @Profile("test") @Bean fun soundZonesDetectorFactorySubscriberTest(): () -> SoundZonesDetector = { soundZonesDetectorSubscriberTest() }
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun soundZonesDetectorSubscriberTest(): SoundZonesDetector =
        SoundZonesDetector(propsUtils(), audioClipTopicSubscriberTest(), audioSignalRmsDaoSubscriberTest())

    @Profile("test") @Bean fun audioSignalRmsDaoSubscriberTest(): AudioSignalRmsDao =
        AudioSignalRmsDaoImpl(propsUtils(), redisConfig.audioSignalRmsDaoTemplateTest(), cassandraTemplateSubscriberTest())

    @Profile("test") @Bean fun cassandraTemplateSubscriberTest(): CassandraOperations = mock(CassandraOperations::class.java)

    @Profile("test") @Bean fun audioClipTopicSubscriberTest() = Topic<AudioClipInfo>()

    @Profile("test") @Bean fun audioClipSubscriberMockSubscriberTest(): Subscriber<AudioClipInfo> =
        AudioClipInfoSubscriberMock(propsUtils(), audioClipTopicSubscriberTest())
}

@Configuration
@Import(value = [TestRedisConfig::class])
class AudioClipSubscriberConfig {

    @Autowired
    private lateinit var redisConfig: TestRedisConfig

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()

    @Profile("test") @Bean fun exceptionHandler(): ExceptionHandler = mock(ExceptionHandler::class.java)

    @Profile("test") @Bean fun audioClipTopicSubscriberTest() = Topic<AudioClipInfo>()

    @Profile("test") @Bean fun audioClipSubscriberTest(): Subscriber<AudioClipInfo> =
        AudioClipInfoSubscriber(propsUtils(), exceptionHandler(), audioClipTopicSubscriberTest(), audioClipDaoTest(), clipGeneratorActorSubscriberTest())

    @Profile("test") @Bean fun clipGeneratorActorSubscriberTest(): ClipGeneratorActor =
        ClipGeneratorActorImpl(propsUtils(), exceptionHandler(), clipGeneratorFactorySubscriberTest(), audioClipDaoTest())

    // ClipGenerator is a prototype bean

    @Profile("test") @Bean fun clipGeneratorFactorySubscriberTest(): () -> ClipGenerator = { clipGeneratorSubscriberTest() }
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun clipGeneratorSubscriberTest(): ClipGenerator =
        ClipGenerator(propsUtils(), audioClipSignalTopicSubscriberTest(), audioSignalDaoTest(), audioClipDaoTest())

    @Profile("test") @Bean fun audioSignalDaoTest(): AudioSignalDao =
        AudioSignalDaoImpl(propsUtils(), redisConfig.audioSignalDaoTemplateTest(), cassandraTemplateSubscriberTest())

    @Profile("test") @Bean fun audioClipDaoTest(): AudioClipDao =
        AudioClipDaoImpl(propsUtils(), redisConfig.audioClipDaoTemplateTest(), cassandraTemplateSubscriberTest())

    @Profile("test") @Bean fun cassandraTemplateSubscriberTest(): CassandraOperations = mock(CassandraOperations::class.java)

    @Profile("test") @Bean fun audioClipSignalTopicSubscriberTest() = Topic<AudioClipSignal>()

    @Profile("test") @Bean fun audioClipSignalSubscriberMockTest(): Subscriber<AudioClipSignal> =
        AudioClipSignalSubscriberMock(propsUtils(), audioClipSignalTopicSubscriberTest(), audioSignalDaoTest(), audioClipDaoTest())
}
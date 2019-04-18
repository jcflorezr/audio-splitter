package net.jcflorezr.config

import net.jcflorezr.broker.AudioClipInfoSubscriber
import net.jcflorezr.broker.AudioClipInfoSubscriberMock
import net.jcflorezr.broker.AudioClipSignalSubscriberMock
import net.jcflorezr.broker.SignalRmsSubscriber
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
import net.jcflorezr.rms.SoundZonesDetectorActorImpl
import net.jcflorezr.util.PropsUtils
import org.mockito.Mockito
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

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()

    @Profile("test") @Bean fun exceptionHandler(): ExceptionHandler = Mockito.mock(ExceptionHandler::class.java)

    @Profile("test") @Bean fun signalRmsTopicSubscriberTest() = Topic<AudioSignalsRmsInfo>()

    @Profile("test") @Bean fun signalRmsSubscriberSubscriberTest(): Subscriber<AudioSignalsRmsInfo> = SignalRmsSubscriber()

    @Profile("test") @Bean fun soundZonesDetectorActorSubscriberTest(): SoundZonesDetectorActor = SoundZonesDetectorActorImpl()

    // SoundZonesDetector is a prototype bean

    @Profile("test") @Bean fun soundZonesDetectorFactorySubscriberTest(): () -> SoundZonesDetector = { soundZonesDetectorSubscriberTest() }
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun soundZonesDetectorSubscriberTest(): SoundZonesDetector = SoundZonesDetector()

    @Profile("test") @Bean fun audioSignalRmsDaoSubscriberTest(): AudioSignalRmsDao = AudioSignalRmsDaoImpl()

    @Profile("test") @Bean fun cassandraTemplateSubscriberTest(): CassandraOperations = Mockito.mock(CassandraOperations::class.java)

    @Profile("test") @Bean fun audioClipTopicSubscriberTest() = Topic<AudioClipInfo>()

    @Profile("test") @Bean fun audioClipSubscriberMockSubscriberTest(): Subscriber<AudioClipInfo> = AudioClipInfoSubscriberMock()
}

@Configuration
@Import(value = [TestRedisConfig::class])
class AudioClipSubscriberConfig {

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()

    @Profile("test") @Bean fun exceptionHandler(): ExceptionHandler = Mockito.mock(ExceptionHandler::class.java)

    @Profile("test") @Bean fun audioClipTopicSubscriberTest() = Topic<AudioClipInfo>()

    @Profile("test") @Bean fun audioClipSubscriberTest(): Subscriber<AudioClipInfo> = AudioClipInfoSubscriber()

    @Profile("test") @Bean fun clipGeneratorActorSubscriberTest(): ClipGeneratorActor = ClipGeneratorActorImpl()

    // ClipGenerator is a prototype bean

    @Profile("test") @Bean fun clipGeneratorFactorySubscriberTest(): () -> ClipGenerator = { clipGeneratorSubscriberTest() }
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun clipGeneratorSubscriberTest(): ClipGenerator = ClipGenerator()

    @Profile("test") @Bean fun audioSignalDaoTest(): AudioSignalDao = AudioSignalDaoImpl()

    @Profile("test") @Bean fun audioClipDaoTest(): AudioClipDao = AudioClipDaoImpl()

    @Profile("test") @Bean fun cassandraTemplateSubscriberTest(): CassandraOperations = Mockito.mock(CassandraOperations::class.java)

    @Profile("test") @Bean fun audioClipSignalTopicSubscriberTest() = Topic<AudioClipSignal>()

    @Profile("test") @Bean fun audioClipSignalSubscriberMockTest(): Subscriber<AudioClipSignal> = AudioClipSignalSubscriberMock()
}
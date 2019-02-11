package net.jcflorezr.config

import net.jcflorezr.broker.AudioClipSubscriberMock
import net.jcflorezr.broker.SignalRmsSubscriber
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.AudioSignalRmsDaoImpl
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.signal.SoundZonesDetector
import net.jcflorezr.signal.SoundZonesDetectorActor
import net.jcflorezr.signal.SoundZonesDetectorActorImpl
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

    @Profile("test") @Bean fun audioClipSubscriberMockSubscriberTest(): Subscriber<AudioClipInfo> = AudioClipSubscriberMock()

}
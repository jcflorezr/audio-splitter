package net.jcflorezr.config

import net.jcflorezr.broker.AudioClipInfoSubscriberMock
import net.jcflorezr.broker.SignalRmsSubscriberMock
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.exception.ExceptionHandler
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.rms.SoundZonesDetector
import org.mockito.Mockito.mock
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope

@Configuration
class TestSoundZonesDetectorConfig {

    @Profile("test") @Bean fun exceptionHandler(): ExceptionHandler = mock(ExceptionHandler::class.java)

    @Profile("test") @Bean fun signalRmsTopicTest() = Topic<AudioSignalsRmsInfo>()

    @Profile("test") @Bean fun signalRmsSubscriberMockTest(): Subscriber<AudioSignalsRmsInfo> = SignalRmsSubscriberMock()

    // SoundZonesDetector is a prototype

    @Profile("test") @Bean fun soundZonesDetectorFactoryTest(): () -> SoundZonesDetector = { soundZonesDetectorSubscriberTest() }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun soundZonesDetectorSubscriberTest(): SoundZonesDetector = SoundZonesDetector()

    @Profile("test") @Bean fun audioSignalRmsTest(): AudioSignalRmsDao = mock(AudioSignalRmsDao::class.java)

    @Profile("test") @Bean fun audioClipTopicTest() = Topic<AudioClipInfo>()

    @Profile("test") @Bean fun audioClipSubscriberMockTest(): Subscriber<AudioClipInfo> = AudioClipInfoSubscriberMock()
}
package net.jcflorezr.config

import net.jcflorezr.broker.AudioClipSubscriberMock
import net.jcflorezr.broker.SignalRmsSubscriber
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.signal.SoundZonesDetector
import net.jcflorezr.signal.SoundZonesDetectorActor
import net.jcflorezr.signal.SoundZonesDetectorActorImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Import(value = [SignalRmsDaoConfig::class])
class SignalRmsSubscriberConfig {

    @Profile("test") @Bean fun signalRmsTopicSubscriberTest() = Topic<AudioSignalsRmsInfo>()

    @Profile("test") @Bean fun signalRmsSubscriberSubscriberTest(): Subscriber<AudioSignalsRmsInfo> = SignalRmsSubscriber()

    @Profile("test") @Bean fun soundZonesDetectorActorSubscriberTest(): SoundZonesDetectorActor = SoundZonesDetectorActorImpl()

    @Profile("test") @Bean fun soundZonesDetectorSubscriberTest(): SoundZonesDetector = SoundZonesDetector()

    @Profile("test") @Bean fun audioClipTopicSubscriberTest() = Topic<AudioClipInfo>()

    @Profile("test") @Bean fun audioClipSubscriberMockSubscriberTest(): Subscriber<AudioClipInfo> = AudioClipSubscriberMock()

}
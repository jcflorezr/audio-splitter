package net.jcflorezr.config

import biz.source_code.dsp.model.AudioSignalKt
import biz.source_code.dsp.sound.AudioIo
import biz.source_code.dsp.sound.AudioIoImpl
import net.jcflorezr.broker.AudioClipSubscriberMock
import net.jcflorezr.broker.SignalRmsSubscriberMock
import net.jcflorezr.broker.SignalSubscriberMock
import net.jcflorezr.broker.SourceFileSubscriberMock
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.core.AudioSplitter
import net.jcflorezr.core.AudioSplitterImpl
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.signal.RmsCalculator
import net.jcflorezr.signal.RmsCalculatorImpl
import net.jcflorezr.signal.SoundZonesDetector
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Profile

@Configuration
@EnableAspectJAutoProxy
class TestRootConfig {

    /*
    Services
     */

    @Profile("test") @Bean fun audioSplitterTest(): AudioSplitter = AudioSplitterImpl()

    @Profile("test") @Bean fun audioIoTest(): AudioIo = AudioIoImpl()

    @Profile("test") @Bean fun rmsCalculatorTest(): RmsCalculator = RmsCalculatorImpl()

    @Profile("test") @Bean fun soundZonesDetectorTest(): SoundZonesDetector = SoundZonesDetector()

    /*
    Topics
     */

    @Profile("test") @Bean fun sourceFileTopicTest() = Topic<InitialConfiguration>()

    @Profile("test") @Bean fun signalTopicTest() = Topic<AudioSignalKt>()

    @Profile("test") @Bean fun signalRmsTopicTest() = Topic<AudioSignalsRmsInfo>()

    @Profile("test") @Bean fun audioClipTopicTest() = Topic<AudioClipInfo>()

    /*
    Subscribers Mocks
     */

    @Profile("test") @Bean fun sourceFileSubscriberTest(): Subscriber<InitialConfiguration> = SourceFileSubscriberMock()

    @Profile("test") @Bean fun signalSubscriberTest(): Subscriber<AudioSignalKt> = SignalSubscriberMock()

    @Profile("test") @Bean fun signalRmsSubscriberTest(): Subscriber<AudioSignalsRmsInfo> = SignalRmsSubscriberMock()

    @Profile("test") @Bean fun audioClipSubscriberTest(): Subscriber<AudioClipInfo> = AudioClipSubscriberMock()

}
package net.jcflorezr.config

import net.jcflorezr.signal.AudioIo
import net.jcflorezr.signal.AudioIoImpl
import net.jcflorezr.broker.AudioClipInfoSubscriberMock
import net.jcflorezr.broker.SignalRmsSubscriberMock
import net.jcflorezr.broker.SignalSubscriberMock
import net.jcflorezr.broker.SourceFileSubscriberMock
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.entrypoint.AudioSplitter
import net.jcflorezr.entrypoint.AudioSplitterImpl
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalKt
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.rms.RmsCalculator
import net.jcflorezr.rms.RmsCalculatorImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class TestRootConfig {

    /*
    Services
     */

    @Profile("test") @Bean fun audioSplitterTest(): AudioSplitter = AudioSplitterImpl()

    @Profile("test") @Bean fun audioIoTest(): AudioIo = AudioIoImpl()

    @Profile("test") @Bean fun rmsCalculatorTest(): RmsCalculator = RmsCalculatorImpl()

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

    @Profile("test") @Bean fun audioClipSubscriberTest(): Subscriber<AudioClipInfo> = AudioClipInfoSubscriberMock()

}
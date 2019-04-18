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
import net.jcflorezr.exception.ExceptionHandler
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.rms.RmsCalculator
import net.jcflorezr.rms.RmsCalculatorImpl
import net.jcflorezr.storage.BucketClient
import net.jcflorezr.util.PropsUtils
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class TestRootConfig {

    /*
    Services
     */

    @Profile("test") @Bean fun bucketClient(): BucketClient = mock(BucketClient::class.java)

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()

    @Profile("test") @Bean fun exceptionHandler(): ExceptionHandler = mock(ExceptionHandler::class.java)

    @Profile("test") @Bean fun audioSplitterTest(): AudioSplitter = AudioSplitterImpl()

    @Profile("test") @Bean fun audioIoTest(): AudioIo = AudioIoImpl()

    @Profile("test") @Bean fun rmsCalculatorTest(): RmsCalculator = RmsCalculatorImpl()

    /*
    Topics
     */

    @Profile("test") @Bean fun sourceFileTopicTest() = Topic<InitialConfiguration>()

    @Profile("test") @Bean fun signalTopicTest() = Topic<AudioSignal>()

    @Profile("test") @Bean fun signalRmsTopicTest() = Topic<AudioSignalsRmsInfo>()

    @Profile("test") @Bean fun audioClipTopicTest() = Topic<AudioClipInfo>()

    /*
    Subscribers Mocks
     */

    @Profile("test") @Bean fun sourceFileSubscriberTest(): Subscriber<InitialConfiguration> = SourceFileSubscriberMock()

    @Profile("test") @Bean fun signalSubscriberTest(): Subscriber<AudioSignal> = SignalSubscriberMock()

    @Profile("test") @Bean fun signalRmsSubscriberTest(): Subscriber<AudioSignalsRmsInfo> = SignalRmsSubscriberMock()

    @Profile("test") @Bean fun audioClipSubscriberTest(): Subscriber<AudioClipInfo> = AudioClipInfoSubscriberMock()
}
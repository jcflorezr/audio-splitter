package net.jcflorezr.config

import biz.source_code.dsp.model.AudioSignalKt
import biz.source_code.dsp.sound.AudioIo
import biz.source_code.dsp.sound.AudioIoImpl
import net.jcflorezr.broker.AudioClip
import net.jcflorezr.broker.AudioClipSubscriberTest
import net.jcflorezr.broker.Signal
import net.jcflorezr.broker.SignalRms
import net.jcflorezr.broker.SignalRmsSubscriberTest
import net.jcflorezr.broker.SignalSubscriberTest
import net.jcflorezr.broker.SourceFile
import net.jcflorezr.broker.SourceFileSubscriberTest
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.core.AudioSplitter
import net.jcflorezr.core.AudioSplitterImpl
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.signal.RmsCalculator
import net.jcflorezr.signal.RmsCalculatorImpl
import net.jcflorezr.signal.SoundZonesDetector
import net.jcflorezr.signal.SoundZonesDetectorImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestRootConfig {

    /*
    Services
     */

    @Bean fun audioSplitter(): AudioSplitter = AudioSplitterImpl()

    @Bean fun audioIo(): AudioIo = AudioIoImpl()

    @Bean fun rmsCalculator(): RmsCalculator = RmsCalculatorImpl()

    @Bean fun soundZonesDetector(): SoundZonesDetector = SoundZonesDetectorImpl()

    /*
    Topics
     */

    @Bean fun sourceFileTopic(): Topic<InitialConfiguration> = SourceFile()

    @Bean fun signalTopic(): Topic<AudioSignalKt> = Signal()

    @Bean fun signalRmsTopic(): Topic<AudioSignalRmsInfoKt> = SignalRms()

    @Bean fun audioClipTopic(): Topic<AudioClipInfo> = AudioClip()

    /*
    Subscribers
     */

    @Bean fun sourceFileSubscriber(): Subscriber = SourceFileSubscriberTest()

    @Bean fun signalSubscriber(): Subscriber = SignalSubscriberTest()

    @Bean fun signalRmsSubscriber(): Subscriber = SignalRmsSubscriberTest()

    @Bean fun audioClipSubscriber(): Subscriber = AudioClipSubscriberTest()

}
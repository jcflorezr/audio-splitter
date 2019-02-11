package net.jcflorezr.config

import biz.source_code.dsp.model.AudioSignalKt
import biz.source_code.dsp.sound.AudioIo
import biz.source_code.dsp.sound.AudioIoImpl
import net.jcflorezr.broker.AudioClipSubscriber
import net.jcflorezr.broker.SignalRmsSubscriber
import net.jcflorezr.broker.SignalSubscriber
import net.jcflorezr.broker.SourceFileSubscriber
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
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class RootConfig {

    /*
    Services
     */

    @Bean fun audioSplitter(): AudioSplitter = AudioSplitterImpl()

    @Bean fun audioIo(): AudioIo = AudioIoImpl()

    @Bean fun rmsCalculator(): RmsCalculator = RmsCalculatorImpl()

    // SoundZonesDetector is a prototype bean

    @Bean fun soundZonesDetectorFactory(): () -> SoundZonesDetector = { soundZonesDetector() }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun soundZonesDetector(): SoundZonesDetector = SoundZonesDetector()


    /*
    Topics
     */

    @Bean fun sourceFileTopic() = Topic<InitialConfiguration>()

    @Bean fun signalTopic() = Topic<AudioSignalKt>()

    @Bean fun signalRmsTopic() = Topic<AudioSignalsRmsInfo>()

    @Bean fun audioClipTopic() = Topic<AudioClipInfo>()

    /*
    Subscribers
     */

    @Bean fun sourceFileSubscriber(): Subscriber<InitialConfiguration> = SourceFileSubscriber()

    @Bean fun signalSubscriber(): Subscriber<AudioSignalKt> = SignalSubscriber()

    @Bean fun signalRmsSubscriber(): Subscriber<AudioSignalsRmsInfo> = SignalRmsSubscriber()

    @Bean fun audioClipSubscriber(): Subscriber<AudioClipInfo> = AudioClipSubscriber()

}

package net.jcflorezr.config

import biz.source_code.dsp.model.AudioSignalKt
import biz.source_code.dsp.sound.AudioIo
import biz.source_code.dsp.sound.AudioIoImpl
import net.jcflorezr.broker.AudioClipLauncher
import net.jcflorezr.broker.AudioClipSubscriber
import net.jcflorezr.broker.AudioSignalLauncher
import net.jcflorezr.broker.AudioSignalRmsLauncher
import net.jcflorezr.broker.SignalRmsSubscriber
import net.jcflorezr.broker.SignalSubscriber
import net.jcflorezr.broker.SourceFileLauncher
import net.jcflorezr.broker.SourceFileSubscriber
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.core.AudioSplitter
import net.jcflorezr.core.AudioSplitterImpl
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.signal.RmsCalculator
import net.jcflorezr.signal.RmsCalculatorImpl
import net.jcflorezr.signal.SoundZonesDetector
import net.jcflorezr.signal.SoundZonesDetectorImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RootConfig {

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

    @Bean fun sourceFileTopic() = Topic<InitialConfiguration>()

    @Bean fun signalTopic() = Topic<AudioSignalKt>()

    @Bean fun signalRmsTopic() = Topic<AudioSignalRmsInfoKt>()

    @Bean fun audioClipTopic() = Topic<AudioClipInfo>()


    /*
    Message Launchers
     */

    @Bean fun sourceFileLauncher() = SourceFileLauncher()

    @Bean fun signalLauncher() = AudioSignalLauncher()

    @Bean fun signalRmsLauncher() = AudioSignalRmsLauncher()

    @Bean fun audioClipLauncher() = AudioClipLauncher()

    /*
    Subscribers
     */

    @Bean fun sourceFileSubscriber(): Subscriber = SourceFileSubscriber()

    @Bean fun signalSubscriber(): Subscriber = SignalSubscriber()

    @Bean fun signalRmsSubscriber(): Subscriber = SignalRmsSubscriber()

    @Bean fun audioClipSubscriber(): Subscriber = AudioClipSubscriber()

}

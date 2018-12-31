package net.jcflorezr.config

import biz.source_code.dsp.model.AudioSignalKt
import biz.source_code.dsp.sound.AudioIo
import biz.source_code.dsp.sound.AudioIoImpl
import net.jcflorezr.broker.AudioClipLauncherTest
import net.jcflorezr.broker.AudioClipSubscriberTest
import net.jcflorezr.broker.AudioSignalLauncherTest
import net.jcflorezr.broker.AudioSignalRmsLauncherTest
import net.jcflorezr.broker.SignalRmsSubscriberTest
import net.jcflorezr.broker.SignalSubscriberTest
import net.jcflorezr.broker.SourceFileLauncherTest
import net.jcflorezr.broker.SourceFileSubscriberTest
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.core.AudioSplitter
import net.jcflorezr.core.AudioSplitterImpl
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalDaoImpl
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.AudioSignalRmsDaoImpl
import net.jcflorezr.dao.SourceFileDao
import net.jcflorezr.dao.SourceFileDaoImpl
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.signal.RmsCalculator
import net.jcflorezr.signal.RmsCalculatorImpl
import net.jcflorezr.signal.SoundZonesDetector
import net.jcflorezr.signal.SoundZonesDetectorImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class TestRootConfig {

    /*
    Services
     */

    @Profile("test") @Bean fun audioSplitter(): AudioSplitter = AudioSplitterImpl()

    @Profile("test") @Bean fun audioIo(): AudioIo = AudioIoImpl()

    @Profile("test") @Bean fun rmsCalculator(): RmsCalculator = RmsCalculatorImpl()

    @Profile("test") @Bean fun soundZonesDetector(): SoundZonesDetector = SoundZonesDetectorImpl()

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

    @Profile("test") @Bean fun sourceFileLauncher() = SourceFileLauncherTest()

    @Profile("test") @Bean fun signalLauncher() = AudioSignalLauncherTest()

    @Profile("test") @Bean fun signalRmsLauncher() = AudioSignalRmsLauncherTest()

    @Profile("test") @Bean fun audioClipLauncher() = AudioClipLauncherTest()

    /*
    Subscribers
     */

    @Profile("test") @Bean fun sourceFileSubscriber(): Subscriber = SourceFileSubscriberTest()

    @Profile("test") @Bean fun signalSubscriber(): Subscriber = SignalSubscriberTest()

    @Profile("test") @Bean fun signalRmsSubscriber(): Subscriber = SignalRmsSubscriberTest()

    @Profile("test") @Bean fun audioClipSubscriber(): Subscriber = AudioClipSubscriberTest()

}
package net.jcflorezr.config

import biz.source_code.dsp.model.AudioClipSignal
import biz.source_code.dsp.model.AudioSignalKt
import biz.source_code.dsp.signal.AudioIo
import biz.source_code.dsp.signal.AudioIoImpl
import net.jcflorezr.broker.AudioClipSignalSubscriber
import net.jcflorezr.broker.AudioClipInfoSubscriber
import net.jcflorezr.broker.SignalRmsSubscriber
import net.jcflorezr.broker.SignalSubscriber
import net.jcflorezr.broker.SourceFileSubscriber
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.clip.ClipGenerator
import net.jcflorezr.clip.ClipGeneratorActor
import net.jcflorezr.clip.ClipGeneratorActorImpl
import net.jcflorezr.facade.AudioSplitter
import net.jcflorezr.facade.AudioSplitterImpl
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalsRmsInfo
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.rms.RmsCalculator
import net.jcflorezr.rms.RmsCalculatorImpl
import net.jcflorezr.rms.SoundZonesDetector
import net.jcflorezr.rms.SoundZonesDetectorActor
import net.jcflorezr.rms.SoundZonesDetectorActorImpl
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

    // ClipGenerator is a prototype

    @Bean fun clipGeneratorFactory(): () -> ClipGenerator = { clipGenerator() }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun clipGenerator(): ClipGenerator = ClipGenerator()

    /*
    Topics
     */

    @Bean fun sourceFileTopic() = Topic<InitialConfiguration>()

    @Bean fun signalTopic() = Topic<AudioSignalKt>()

    @Bean fun signalRmsTopic() = Topic<AudioSignalsRmsInfo>()

    @Bean fun audioClipInfoTopic() = Topic<AudioClipInfo>()

    @Bean fun audioClipSignalTopic() = Topic<AudioClipSignal>()

    /*
    Subscribers
     */

    @Bean fun sourceFileSubscriber(): Subscriber<InitialConfiguration> = SourceFileSubscriber()

    @Bean fun signalSubscriber(): Subscriber<AudioSignalKt> = SignalSubscriber()

    @Bean fun signalRmsSubscriber(): Subscriber<AudioSignalsRmsInfo> = SignalRmsSubscriber()

    @Bean fun audioClipInfoSubscriber(): Subscriber<AudioClipInfo> = AudioClipInfoSubscriber()

    @Bean fun audioClipSignalSubscriber(): Subscriber<AudioClipSignal> = AudioClipSignalSubscriber()

    /*
    Actors
     */

    @Bean fun soundZonesDetectorActor(): SoundZonesDetectorActor = SoundZonesDetectorActorImpl()

    @Bean fun clipGeneratorActor(): ClipGeneratorActor = ClipGeneratorActorImpl()

}

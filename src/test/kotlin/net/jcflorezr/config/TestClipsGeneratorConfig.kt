package net.jcflorezr.config

import net.jcflorezr.signal.AudioIoImpl
import net.jcflorezr.broker.AudioClipSignalSubscriberMock
import net.jcflorezr.broker.Subscriber
import net.jcflorezr.broker.Topic
import net.jcflorezr.clip.ClipGenerator
import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioClipDaoImpl
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalDaoImpl
import net.jcflorezr.model.AudioClipSignal
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.util.PropsUtils
import org.mockito.Mockito.mock
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.Scope
import org.springframework.data.cassandra.core.CassandraOperations

@Configuration
@Import(value = [TestRedisConfig::class])
class TestClipsGeneratorConfig {

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()

    @Profile("test") @Bean fun audioIoTest() = AudioIoImpl()

    @Profile("test") @Bean fun signalTopicTest() = Topic<AudioSignal>()

    // ClipGenerator is a prototype

    @Profile("test") @Bean fun clipGeneratorFactoryTest(): () -> ClipGenerator = { clipGeneratorTest() }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun clipGeneratorTest(): ClipGenerator = ClipGenerator()

    @Profile("test") @Bean fun audioSignalDaoTest(): AudioSignalDao = AudioSignalDaoImpl()

    @Profile("test") @Bean fun audioClipDaoTest(): AudioClipDao = AudioClipDaoImpl()

    @Profile("test") @Bean fun cassandraTemplateTest(): CassandraOperations = mock(CassandraOperations::class.java)

    @Profile("test") @Bean fun audioClipSignalTopicTest() = Topic<AudioClipSignal>()

    @Profile("test") @Bean fun audioClipSignalSubscriberTest(): Subscriber<AudioClipSignal> = AudioClipSignalSubscriberMock()
}
package net.jcflorezr.config

import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioClipDaoImpl
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalDaoImpl
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.AudioSignalRmsDaoImpl
import net.jcflorezr.dao.SourceFileDao
import net.jcflorezr.dao.SourceFileDaoImpl
import net.jcflorezr.util.PropsUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Import(value = [TestCassandraConfig::class])
class TestSourceFileDaoConfig {

    @Autowired
    private lateinit var cassandraConfig: TestCassandraConfig

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()
    @Profile("test") @Bean fun sourceFileDao(): SourceFileDao =
        SourceFileDaoImpl(propsUtils(), cassandraConfig.cassandraCustomTemplateTest())
}

@Configuration
@Import(value = [TestCassandraConfig::class, TestRedisConfig::class])
class TestSignalDaoConfig {

    @Autowired
    private lateinit var cassandraConfig: TestCassandraConfig
    @Autowired
    private lateinit var redisConfig: TestRedisConfig

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()
    @Profile("test") @Bean fun audioSignalDao(): AudioSignalDao =
        AudioSignalDaoImpl(propsUtils(), redisConfig.audioSignalDaoTemplateTest(), cassandraConfig.cassandraCustomTemplateTest())
}

@Configuration
@Import(value = [TestCassandraConfig::class, TestRedisConfig::class])
class TestSignalRmsDaoConfig {

    @Autowired
    private lateinit var cassandraConfig: TestCassandraConfig
    @Autowired
    private lateinit var redisConfig: TestRedisConfig

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()
    @Profile("test") @Bean fun audioSignalRmsDao(): AudioSignalRmsDao =
        AudioSignalRmsDaoImpl(propsUtils(), redisConfig.audioSignalRmsDaoTemplateTest(), cassandraConfig.cassandraCustomTemplateTest())
}

@Configuration
@Import(value = [TestCassandraConfig::class, TestRedisConfig::class])
class TestClipDaoConfig {

    @Autowired
    private lateinit var cassandraConfig: TestCassandraConfig
    @Autowired
    private lateinit var redisConfig: TestRedisConfig

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()
    @Profile("test") @Bean fun audioClipDao(): AudioClipDao =
        AudioClipDaoImpl(propsUtils(), redisConfig.audioClipDaoTemplateTest(), cassandraConfig.cassandraCustomTemplateTest())
}
package net.jcflorezr.config

import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalDaoImpl
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.AudioSignalRmsDaoImpl
import net.jcflorezr.dao.SourceFileDao
import net.jcflorezr.dao.SourceFileDaoImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Import(value = [TestCassandraConfig::class])
class SourceFileDaoConfig {

    @Profile("test") @Bean fun sourceFileDao(): SourceFileDao = SourceFileDaoImpl()

}

@Configuration
@Import(value = [TestCassandraConfig::class, TestRedisConfig::class])
class SignalDaoConfig {

    @Profile("test") @Bean fun audioSignalDao(): AudioSignalDao = AudioSignalDaoImpl()

}

@Configuration
@Import(value = [TestRedisConfig::class])
class SignalRmsDaoConfig {

    @Profile("test") @Bean fun audioSignalRmsDao(): AudioSignalRmsDao = AudioSignalRmsDaoImpl()

}
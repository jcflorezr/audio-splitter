package net.jcflorezr.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.dao.AudioClipDao
import net.jcflorezr.dao.AudioClipDaoImpl
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.dao.AudioSignalDao
import net.jcflorezr.dao.AudioSignalDaoImpl
import net.jcflorezr.dao.AudioSignalRmsDao
import net.jcflorezr.dao.AudioSignalRmsDaoImpl
import net.jcflorezr.dao.SourceFileDao
import net.jcflorezr.dao.SourceFileDaoImpl
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignal
import net.jcflorezr.util.PropsUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.Scope
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.SerializationException
import org.springframework.lang.Nullable

@Configuration
@PropertySource(value = ["classpath:config/redis.properties"])
class RedisConfig {

    @Autowired
    private lateinit var cassandraConfig: CassandraConfig

    @Autowired
    private lateinit var redisConfig: RedisConfig

    @Value("\${redis.hostname}")
    private lateinit var hostName: String
    @Value("\${redis.port}")
    private lateinit var port: Integer

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun jedisConnectionFactory(): JedisConnectionFactory {
        return JedisConnectionFactory(RedisStandaloneConfiguration(hostName, port.toInt()))
    }

    @Bean
    fun audioSignalDaoTemplate(): RedisTemplate<String, AudioSignal> {
        val template = RedisTemplate<String, AudioSignal>()
        template.setConnectionFactory(jedisConnectionFactory())
        template.valueSerializer = Jackson2JsonRedisSerializerKotlin(AudioSignal::class.java)
        return template
    }

    @Bean
    fun audioSignalRmsDaoTemplate(): RedisTemplate<String, AudioSignalRmsInfo> {
        val template = RedisTemplate<String, AudioSignalRmsInfo>()
        template.setConnectionFactory(jedisConnectionFactory())
        template.valueSerializer = Jackson2JsonRedisSerializerKotlin(AudioSignalRmsInfo::class.java)
        return template
    }

    @Bean
    fun audioClipDaoTemplateTest(): RedisTemplate<String, AudioClipInfo> {
        val template = RedisTemplate<String, AudioClipInfo>()
        template.setConnectionFactory(jedisConnectionFactory())
        template.valueSerializer = Jackson2JsonRedisSerializerKotlin(AudioClipInfo::class.java)
        return template
    }

    /*
    DAOs
     */

    fun propsUtils(): PropsUtils = PropsUtils()

    @Bean fun sourceFileDao(): SourceFileDao =
        SourceFileDaoImpl(propsUtils(), cassandraConfig.cassandraCustomTemplate())

    @Bean fun audioSignalDao(): AudioSignalDao =
        AudioSignalDaoImpl(propsUtils(), redisConfig.audioSignalDaoTemplate(), cassandraConfig.cassandraCustomTemplate())

    @Bean fun audioSignalRmsDao(): AudioSignalRmsDao =
        AudioSignalRmsDaoImpl(propsUtils(), redisConfig.audioSignalRmsDaoTemplate(), cassandraConfig.cassandraCustomTemplate())

    @Bean fun audioClipDao(): AudioClipDao =
        AudioClipDaoImpl(propsUtils(), redisConfig.audioClipDaoTemplateTest(), cassandraConfig.cassandraCustomTemplate())
}

/**
 * Subclass of Jackson Serializer for Redis which does
 * contain a Jackson mapper for Kotlin classes
 */
class Jackson2JsonRedisSerializerKotlin<T>(
    private val classType: Class<T>
) : Jackson2JsonRedisSerializer<T>(classType) {

    private val objectMapper = ObjectMapper().registerKotlinModule()

    override fun deserialize(@Nullable bytes: ByteArray?): T {
        if (bytes == null || bytes.isEmpty()) {
            return classType.newInstance()
        }
        try {
            return this.objectMapper.readValue(bytes, 0, bytes.size, getJavaType(classType)) as T
        } catch (ex: Exception) {
            throw SerializationException("Could not read JSON: " + ex.message, ex)
        }
    }

    override fun serialize(@Nullable t: Any?): ByteArray {
        if (t == null) {
            return ByteArray(0)
        }
        try {
            return this.objectMapper.writeValueAsBytes(t)
        } catch (ex: Exception) {
            throw SerializationException("Could not write JSON: " + ex.message, ex)
        }
    }
}

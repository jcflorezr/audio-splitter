package net.jcflorezr.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.model.AudioSignalRmsInfo
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalKt
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.Scope
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.SerializationException
import org.springframework.lang.Nullable

@Configuration
@PropertySource(value = ["classpath:config/test-redis.properties"])
class TestRedisConfig {

    @Value("\${redis.hostname}")
    private lateinit var hostName: String
    @Value("\${redis.port}")
    private lateinit var port: Integer

    @Profile("test")
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun jedisConnectionFactory(): JedisConnectionFactory {

        /*
         TODO: currently a new connection is open for each redis transaction,
            let us investigate how to open a single connection for all transactions
          */

//        val poolConfig = JedisPoolConfig()
//        poolConfig.maxTotal = 128
//        poolConfig.maxIdle = 128
//        poolConfig.minIdle = 16
//        poolConfig.testOnBorrow = true
//        poolConfig.testOnReturn = true
//        poolConfig.testWhileIdle = true
//        poolConfig.minEvictableIdleTimeMillis = Duration.ofSeconds(60).toMillis()
//        poolConfig.timeBetweenEvictionRunsMillis = Duration.ofSeconds(30).toMillis()
//        poolConfig.numTestsPerEvictionRun = 3
//        poolConfig.blockWhenExhausted = true
//
//        val factory = JedisConnectionFactory(poolConfig)
//        factory.hostName = hostName
//        factory.usePool = true
//        factory.port = port.toInt()
//        return factory

        return JedisConnectionFactory(
            RedisStandaloneConfiguration(hostName, port.toInt())
        )
    }

    @Profile("test")
    @Bean
    fun audioSignalDaoTemplateTest(): RedisTemplate<String, AudioSignalKt> {
        val template = RedisTemplate<String, AudioSignalKt>()
        template.setConnectionFactory(jedisConnectionFactory())
        template.valueSerializer = Jackson2JsonRedisSerializerKotlin(AudioSignalKt::class.java)
        return template
    }

    @Profile("test")
    @Bean
    fun audioSignalRmsDaoTemplateTest(): RedisTemplate<String, AudioSignalRmsInfo> {
        val template = RedisTemplate<String, AudioSignalRmsInfo>()
        template.setConnectionFactory(jedisConnectionFactory())
        template.valueSerializer = Jackson2JsonRedisSerializerKotlin(AudioSignalRmsInfo::class.java)
        return template
    }

    @Profile("test")
    @Bean
    fun audioClipDaoTemplateTest(): RedisTemplate<String, AudioClipInfo> {
        val template = RedisTemplate<String, AudioClipInfo>()
        template.setConnectionFactory(jedisConnectionFactory())
        template.valueSerializer = Jackson2JsonRedisSerializerKotlin(AudioClipInfo::class.java)
        return template
    }

}


/**
 * Subclass of Jackson Serializer for Redis, this serializer
 * does supply a Jackson mapper for Kotlin classes
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

    @Throws(SerializationException::class)
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

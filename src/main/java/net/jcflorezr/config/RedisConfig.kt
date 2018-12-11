package net.jcflorezr.config

import biz.source_code.dsp.model.AudioSignalKt
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.persistence.AudioSignalDao
import net.jcflorezr.persistence.AudioSignalDaoImpl
import net.jcflorezr.persistence.AudioSignalRmsDao
import net.jcflorezr.persistence.AudioSignalRmsDaoImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.SerializationException
import org.springframework.lang.Nullable

@Configuration
@PropertySource(value = ["classpath:config/redis.properties"])
class RedisConfig {

    @Value("\${redis.hostname}")
    private lateinit var hostName: String
    @Value("\${redis.port}")
    private lateinit var port: Integer

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        return JedisConnectionFactory(
            RedisStandaloneConfiguration(hostName, port.toInt()))
    }

    @Bean
    fun audioSignalDaoTemplate(): RedisTemplate<String, AudioSignalKt> {
        val template = RedisTemplate<String, AudioSignalKt>()
        template.setConnectionFactory(jedisConnectionFactory())
        template.valueSerializer = Jackson2JsonRedisSerializerKotlin(AudioSignalKt::class.java)
        return template
    }

    @Bean
    fun audioSignalRmsDaoTemplate(): RedisTemplate<String, AudioSignalRmsInfoKt> {
        val template = RedisTemplate<String, AudioSignalRmsInfoKt>()
        template.setConnectionFactory(jedisConnectionFactory())
        template.valueSerializer = Jackson2JsonRedisSerializerKotlin(AudioSignalRmsInfoKt::class.java)
        return template
    }

    /*
    DAOs
     */

    @Bean fun audioSignalDao(): AudioSignalDao = AudioSignalDaoImpl()

    @Bean fun audioSignalRmsDao(): AudioSignalRmsDao = AudioSignalRmsDaoImpl()

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

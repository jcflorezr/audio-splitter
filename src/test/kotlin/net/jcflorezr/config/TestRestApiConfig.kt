package net.jcflorezr.config

import net.jcflorezr.broker.Topic
import net.jcflorezr.exception.ExceptionHandler
import net.jcflorezr.storage.BucketClient
import net.jcflorezr.util.PropsUtils
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class TestRestApiConfig {

    @Profile("test") @Bean fun bucketClient(): BucketClient = Mockito.mock(BucketClient::class.java)

    @Profile("test") @Bean fun propsUtils(): PropsUtils = PropsUtils()

    @Profile("test") @Bean fun exceptionHandler(): ExceptionHandler = Mockito.mock(ExceptionHandler::class.java)

    @Profile("test") @Bean fun sourceFileTopicTest() = mock(Topic::class.java)!!
}
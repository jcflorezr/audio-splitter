package net.jcflorezr.config

import net.jcflorezr.broker.Topic
import net.jcflorezr.entrypoint.ApiErrorHandler
import net.jcflorezr.entrypoint.AudioSplitterImpl
import net.jcflorezr.exception.ExceptionHandler
import net.jcflorezr.exception.ExceptionHandlerImpl
import net.jcflorezr.model.InitialConfiguration
import net.jcflorezr.storage.BucketClient
import net.jcflorezr.storage.BucketClientImpl
import net.jcflorezr.util.PropsUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {

    fun propsUtils(): PropsUtils = PropsUtils()

    fun messageLauncher(): Topic<InitialConfiguration> = Topic()

    fun bucketClient(): BucketClient = BucketClientImpl()

    fun exceptionHandler(): ExceptionHandler = ExceptionHandlerImpl(propsUtils())

    @Bean fun audioSplitterController() =
        AudioSplitterImpl(propsUtils(), messageLauncher(), bucketClient(), exceptionHandler())

    @Bean fun apiErrorHandler() = ApiErrorHandler()

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON_UTF8)
    }

    override fun configureDefaultServletHandling(configurer: DefaultServletHandlerConfigurer) {
        configurer.enable()
    }
}
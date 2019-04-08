package net.jcflorezr.config

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer

class WebAppInitializer : AbstractAnnotationConfigDispatcherServletInitializer() {

    override fun getRootConfigClasses(): Array<Class<*>>? {
        return arrayOf(RootConfig::class.java, SwaggerConfig::class.java)
    }

    override fun getServletConfigClasses(): Array<Class<*>>? {
        return arrayOf(WebConfig::class.java)
    }

    override fun getServletMappings(): Array<String> {
        return arrayOf("/")
    }
}
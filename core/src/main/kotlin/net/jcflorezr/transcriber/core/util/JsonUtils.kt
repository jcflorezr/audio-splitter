package net.jcflorezr.transcriber.core.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

object JsonUtils {

    val mapper = ObjectMapper().registerKotlinModule()

    inline fun <reified T> fromJsonToObject(jsonFile: File): T = mapper.readValue(jsonFile, T::class.java)

    inline fun <reified T> fromJsonToList(jsonFile: File): List<T> {
        val listType = mapper.typeFactory.constructCollectionType(List::class.java, T::class.java)
        return mapper.readValue(jsonFile, listType)
    }
}

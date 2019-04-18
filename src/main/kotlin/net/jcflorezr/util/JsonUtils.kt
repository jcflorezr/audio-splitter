package net.jcflorezr.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonUtils {

    val MAPPER = ObjectMapper().registerKotlinModule()

    fun <T> convertStringToPojo(json: String, pojoClass: Class<T>) = MAPPER.readValue(json, pojoClass)

    fun <T> convertMapToPojo(map: Map<*, *>, pojoClass: Class<T>) = MAPPER.convertValue(map, pojoClass)!!

    fun convertMapToJsonAsString(map: Map<*, *>) = MAPPER.writeValueAsString(map)

    fun convertObjectToJsonBytes(classObject: Any): ByteArray {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        return MAPPER.writeValueAsBytes(classObject)
    }
}
package net.jcflorezr.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object JsonUtils {

    private val MAPPER = ObjectMapper().registerKotlinModule()

    fun <T> convertMapToPojo(map: Map<*, *>, pojoClass: Class<T>) = MAPPER.convertValue(map, pojoClass)!!

    fun convertMapToJsonAsString(map: Map<*, *>) = MAPPER.writeValueAsString(map)

}
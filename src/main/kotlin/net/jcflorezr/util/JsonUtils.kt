package net.jcflorezr.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

// TODO: rename it once its java equivalent is removed
object JsonUtilsKt {

    private val MAPPER = ObjectMapper().registerKotlinModule()

    fun <T> convertMapToPojo(map: Map<*, *>, pojoClass: Class<T>): T {
        return MAPPER.convertValue(map, pojoClass)
    }

    fun convertMapToJsonAsString(map: Map<*, *>): String {
        return MAPPER.writeValueAsString(map)
    }

}
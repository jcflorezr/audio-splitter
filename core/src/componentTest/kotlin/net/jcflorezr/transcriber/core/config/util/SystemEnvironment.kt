package net.jcflorezr.transcriber.core.config.util

import java.util.Collections

object SystemEnvironment {

    // WARNING:
    // this method sets new environment variables in the host operating system!
    // use it just for test purposes
    fun setEnvironmentVariable(newEnvironment: Map<String, String>) {
        newEnvironment
            .filter { System.getenv(it.key) == null }
            .let { filteredNewEnvironment ->
                try {
                    val processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment")
                    processEnvironmentClass.getDeclaredField("theEnvironment")
                        .apply { isAccessible = true }
                        .let { field -> field[null] as MutableMap<String, String> }
                        .apply { putAll(filteredNewEnvironment) }
                    processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment")
                        .apply { isAccessible = true }
                        .let { field -> field[null] as MutableMap<String, String> }
                        .apply { putAll(filteredNewEnvironment) }
                } catch (ex: NoSuchFieldException) {
                    val classes: Array<Class<*>> = Collections::class.java.declaredClasses
                    val env = System.getenv()
                    classes
                        .filter { "java.util.Collections\$UnmodifiableMap" == it.name }
                        .map { it.getDeclaredField("m").apply { isAccessible = true } }
                        .forEach { field ->
                            val obj = field[env]
                            val map = obj as MutableMap<String, String>
                            map.clear()
                            map.putAll(filteredNewEnvironment)
                        }
                }
            }
    }
}

package net.jcflorezr.transcriber.core.util

object CollectionUtils {

    fun <T: Any> findDuplicates(list: List<T>): Map<T, Int> = list.groupingBy { it }.eachCount().filter { it.value > 1 }
}
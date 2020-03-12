package net.jcflorezr.transcriber.core.domain

interface EventHandler<T : Event> {

    fun execute(event: T)
}
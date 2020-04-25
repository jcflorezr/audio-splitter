package net.jcflorezr.transcriber.core.domain

interface EventHandler<T : Event<out AggregateRoot>> {
    suspend fun execute(event: T)
}
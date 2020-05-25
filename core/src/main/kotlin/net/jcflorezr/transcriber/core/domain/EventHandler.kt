package net.jcflorezr.transcriber.core.domain

interface EventHandler<in T : Event<AggregateRoot>> {
    suspend fun execute(event: T)
}

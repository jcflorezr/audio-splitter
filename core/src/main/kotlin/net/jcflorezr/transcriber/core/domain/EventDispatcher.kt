package net.jcflorezr.transcriber.core.domain

interface EventDispatcher {
    suspend fun publish(vararg events: Event<AggregateRoot>)
}

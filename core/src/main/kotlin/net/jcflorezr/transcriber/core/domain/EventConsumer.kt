package net.jcflorezr.transcriber.core.domain

interface EventConsumer<T : Event<in AggregateRoot>> {
    fun consume(vararg events: T)
}

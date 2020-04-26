package net.jcflorezr.transcriber.core.domain

interface EventDispatcher {
    fun publish(vararg events: Event<out AggregateRoot>)
}

package net.jcflorezr.transcriber.core.domain

interface EventDispatcher {

    fun publish(event: Event)
}
package net.jcflorezr.broker

import org.springframework.stereotype.Service

interface Message

@Service
final class Topic<T : Message> {

    private val subscribers = mutableListOf<Subscriber<T>>()

    fun register(subscriber: Subscriber<T>) {
        subscribers.add(subscriber)
    }

    fun unregister(subscriber: Subscriber<T>) {
        subscribers.remove(subscriber)
    }

    suspend fun postMessage(message: T) {
        notifyObservers(message)
    }

    private suspend fun notifyObservers(message: T) {
        subscribers.forEach { it.update(message) }
    }
}

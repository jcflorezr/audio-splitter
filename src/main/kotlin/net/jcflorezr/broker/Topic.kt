package net.jcflorezr.broker

import org.springframework.stereotype.Service

interface Message

@Service
final class Topic<T : Message> {

    private val subscribers = mutableListOf<Subscriber>()
    private lateinit var message: T

    fun register(subscriber: Subscriber) {
        subscribers.add(subscriber)
    }

    fun unregister(subscriber: Subscriber) {
        subscribers.remove(subscriber)
    }

    suspend fun postMessage(msg: T) {
        if (!::message.isInitialized || this.message != msg) {
            message = msg
            notifyObservers()
        }
    }

    suspend fun getMessage() = message

    private suspend fun notifyObservers() {
        subscribers.forEach { it.update() }
    }
}

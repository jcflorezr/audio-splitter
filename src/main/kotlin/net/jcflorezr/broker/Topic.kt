package net.jcflorezr.broker

import biz.source_code.dsp.model.AudioSignalKt
import net.jcflorezr.model.AudioClipInfo
import net.jcflorezr.model.AudioSignalRmsInfoKt
import net.jcflorezr.model.InitialConfiguration
import org.springframework.stereotype.Service

abstract class Topic<T> {

    private val observers = mutableListOf<Subscriber>()
    protected val mutex = Any()

    fun register(subscriber: Subscriber) {
        synchronized(mutex) {
            observers.add(subscriber)
        }
    }

    fun unregister(subscriber: Subscriber) {
        synchronized(mutex) {
            observers.remove(subscriber)
        }
    }

    abstract fun postMessage(msg: T)

    abstract fun getMessage() : T

    fun notifyObservers() {
        synchronized(mutex) {
            observers.forEach { it.update() }
        }
    }
}

@Service
final class SourceFile : Topic<InitialConfiguration>() {

    private lateinit var message: InitialConfiguration

    override fun postMessage(message: InitialConfiguration) {
        this.message = message
        notifyObservers()
    }

    override fun getMessage() = message

}

@Service
final class Signal : Topic<AudioSignalKt>() {

    private lateinit var message: AudioSignalKt

    override fun postMessage(msg: AudioSignalKt) {
        synchronized(mutex) {
            if (!::message.isInitialized || this.message != msg) {
                message = msg
                notifyObservers()
            }
        }
    }

    override fun getMessage() = message

}

@Service
final class SignalRms : Topic<AudioSignalRmsInfoKt>() {

    private lateinit var message: AudioSignalRmsInfoKt

    override fun postMessage(msg: AudioSignalRmsInfoKt) {
        synchronized(mutex) {
            if (!::message.isInitialized || this.message != msg) {
                message = msg
                notifyObservers()
            }
        }
    }

    override fun getMessage() = message

}

@Service
final class AudioClip : Topic<AudioClipInfo>() {

    private lateinit var message: AudioClipInfo

    override fun postMessage(msg: AudioClipInfo) {
        synchronized(mutex) {
            if (!::message.isInitialized || this.message != msg) {
                message = msg
                notifyObservers()
            }
        }
    }

    override fun getMessage() = message

}
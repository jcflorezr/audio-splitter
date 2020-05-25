package net.jcflorezr.transcriber.core.domain

abstract class Event<out T : AggregateRoot>(val aggregateRoot: T)

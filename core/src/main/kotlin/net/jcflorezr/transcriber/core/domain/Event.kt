package net.jcflorezr.transcriber.core.domain

abstract class Event<T : AggregateRoot>(val aggregateRoot: T)
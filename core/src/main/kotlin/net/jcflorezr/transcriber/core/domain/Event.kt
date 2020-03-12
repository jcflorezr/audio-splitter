package net.jcflorezr.transcriber.core.domain

interface Event {
    suspend fun execute(aggregateRoot: AggregateRoot)
}
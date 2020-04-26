package net.jcflorezr.transcriber.core.domain

interface Command<T : AggregateRoot> {
    suspend fun execute(aggregateRoot: T)
}
